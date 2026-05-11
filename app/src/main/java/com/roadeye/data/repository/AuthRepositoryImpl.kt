package com.roadeye.data.repository

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.roadeye.domain.model.User
import com.roadeye.domain.model.UserRole
import com.roadeye.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepo"
    }

    override val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    override val isLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    // ─────────────────────────────────────────────────────────────
    // Send OTP via Firebase PhoneAuthProvider
    // ─────────────────────────────────────────────────────────────
    override fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (verificationId: String, resendToken: PhoneAuthProvider.ForceResendingToken) -> Unit,
        onVerificationCompleted: (credential: PhoneAuthCredential) -> Unit,
        onError: (message: String) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted – auto-retrieval")
                onVerificationCompleted(credential)
            }

            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                Log.e(TAG, "onVerificationFailed: ${e.message}")
                val msg = when {
                    e.message?.contains("TOO_SHORT", ignoreCase = true) == true ->
                        "Phone number too short. Enter full 10-digit number."
                    e.message?.contains("INVALID_PHONE_NUMBER", ignoreCase = true) == true ->
                        "Invalid phone number format."
                    e.message?.contains("QUOTA_EXCEEDED", ignoreCase = true) == true ->
                        "SMS quota exceeded. Try again later."
                    e.message?.contains("MISSING_CLIENT_IDENTIFIER", ignoreCase = true) == true ->
                        "SHA-1 fingerprint not registered in Firebase. Add your debug SHA-1."
                    e.message?.contains("BLOCKED", ignoreCase = true) == true ->
                        "Request blocked by Firebase. Check Phone Auth settings."
                    e.message?.contains("API key", ignoreCase = true) == true ->
                        "Invalid API key. Ensure google-services.json is correct."
                    else -> e.message ?: "Failed to send OTP."
                }
                onError(msg)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent ✓")
                onCodeSent(verificationId, token)
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // ─────────────────────────────────────────────────────────────
    // Manual OTP entry → credential → sign in
    // ─────────────────────────────────────────────────────────────
    override suspend fun verifyOtp(verificationId: String, otp: String): Result<User> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            signInWithCredential(credential)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e(TAG, "verifyOtp: invalid credential", e)
            Result.failure(Exception("Incorrect OTP. Please check and try again."))
        } catch (e: Exception) {
            Log.e(TAG, "verifyOtp error", e)
            Result.failure(Exception(friendlyAuthError(e)))
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Core sign-in — separates Auth from Firestore so a Firestore
    // permissions error never masks a successful login.
    // ─────────────────────────────────────────────────────────────
    override suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<User> {
        // STEP 1: Firebase Auth – if this throws, login genuinely failed
        val firebaseUser = try {
            val result = firebaseAuth.signInWithCredential(credential).await()
            result.user ?: return Result.failure(Exception("Sign-in returned null user."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e(TAG, "Auth credential invalid", e)
            return Result.failure(Exception("Incorrect OTP. Please check and try again."))
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Auth failed", e)
            return Result.failure(Exception(friendlyAuthError(e)))
        }

        Log.d(TAG, "Firebase Auth success – uid=${firebaseUser.uid}")

        // STEP 2: Firestore user profile (non-fatal – login still succeeds if Firestore fails)
        val user = buildNewUser(firebaseUser.uid, firebaseUser.phoneNumber ?: "")
        try {
            val docRef = firestore.collection("users").document(firebaseUser.uid)
            val existingDoc = docRef.get().await()
            if (existingDoc.exists()) {
                val existing = existingDoc.toObject(User::class.java)
                if (existing != null) {
                    Log.d(TAG, "Existing user profile loaded")
                    return Result.success(existing)
                }
            }
            // New user – save profile
            docRef.set(user).await()
            Log.d(TAG, "New user profile created")
        } catch (e: Exception) {
            // Firestore failed (permissions, network) – still let the user in.
            // They are authenticated; profile will sync later.
            Log.w(TAG, "Firestore profile save failed (non-fatal): ${e.message}")
        }

        return Result.success(user)
    }

    // ─────────────────────────────────────────────────────────────
    // Officer email/password login
    // ─────────────────────────────────────────────────────────────
    override suspend fun loginOfficer(email: String, password: String): Result<User> {
        // STEP 1: Auth
        val firebaseUser = try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user ?: return Result.failure(Exception("Login returned null user."))
        } catch (e: FirebaseAuthInvalidUserException) {
            return Result.failure(Exception("No account found for this email."))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            return Result.failure(Exception("Incorrect password. Please try again."))
        } catch (e: Exception) {
            Log.e(TAG, "Officer login error", e)
            return Result.failure(Exception(e.message ?: "Login failed."))
        }

        Log.d(TAG, "Officer auth success – uid=${firebaseUser.uid}")

        // STEP 2: Load/create officer profile (non-fatal)
        val officerUser = try {
            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            if (doc.exists()) {
                doc.toObject(User::class.java) ?: defaultOfficerUser(firebaseUser.uid, email)
            } else {
                val newOfficer = defaultOfficerUser(firebaseUser.uid, email)
                firestore.collection("users").document(firebaseUser.uid).set(newOfficer).await()
                newOfficer
            }
        } catch (e: Exception) {
            Log.w(TAG, "Officer Firestore profile failed (non-fatal): ${e.message}")
            defaultOfficerUser(firebaseUser.uid, email)
        }

        return Result.success(officerUser)
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override suspend fun getCurrentUser(): User? {
        val uid = currentUserId ?: return null
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            Log.w(TAG, "getCurrentUser Firestore error (non-fatal): ${e.message}")
            null
        }
    }

    override suspend fun updateFcmToken(token: String) {
        val uid = currentUserId ?: return
        try {
            firestore.collection("users").document(uid).update("fcmToken", token).await()
        } catch (e: Exception) {
            Log.w(TAG, "FCM token update failed (non-fatal): ${e.message}")
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────
    private fun buildNewUser(uid: String, phone: String) = User(
        id = uid,
        phone = phone,
        role = UserRole.CITIZEN,
        createdAt = System.currentTimeMillis()
    )

    private fun defaultOfficerUser(uid: String, email: String) = User(
        id = uid,
        email = email,
        role = UserRole.OFFICER,
        createdAt = System.currentTimeMillis()
    )

    private fun friendlyAuthError(e: Exception): String = when {
        e.message?.contains("session-expired", ignoreCase = true) == true ->
            "OTP expired. Please request a new one."
        e.message?.contains("invalid-verification-code", ignoreCase = true) == true ->
            "Incorrect OTP. Please check and try again."
        e.message?.contains("invalid-verification-id", ignoreCase = true) == true ->
            "Session expired. Please go back and re-send OTP."
        e.message?.contains("network", ignoreCase = true) == true ->
            "Network error. Check your internet connection."
        else -> e.message ?: "Authentication failed. Please try again."
    }
}