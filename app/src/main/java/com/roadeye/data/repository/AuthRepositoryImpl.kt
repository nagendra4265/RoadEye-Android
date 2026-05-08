package com.roadeye.data.repository

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
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

    override val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    override val isLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null

    override suspend fun sendOtp(phoneNumber: String): Result<String> {
        // In real app, implement PhoneAuthProvider callback
        // Returning mock verification ID for demo
        return Result.success("DEMO_VERIFICATION_ID")
    }

    override suspend fun verifyOtp(verificationId: String, otp: String): Result<User> {
        return try {
            // Demo: For testing use credential sign in
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Auth failed"))

            val user = User(
                id = firebaseUser.uid,
                phone = firebaseUser.phoneNumber ?: "",
                role = UserRole.CITIZEN
            )
            // Save to Firestore
            firestore.collection("users").document(user.id).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginOfficer(email: String, password: String): Result<User> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Login failed"))

            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = doc.toObject(User::class.java) ?: User(
                id = firebaseUser.uid,
                email = email,
                role = UserRole.OFFICER
            )
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
            null
        }
    }

    override suspend fun updateFcmToken(token: String) {
        val uid = currentUserId ?: return
        try {
            firestore.collection("users").document(uid)
                .update("fcmToken", token).await()
        } catch (e: Exception) {
            // ignore
        }
    }
}
