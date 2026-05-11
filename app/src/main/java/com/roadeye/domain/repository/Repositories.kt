package com.roadeye.domain.repository

import android.app.Activity
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.roadeye.domain.model.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: String?
    val isLoggedIn: Boolean

    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (verificationId: String, resendToken: PhoneAuthProvider.ForceResendingToken) -> Unit,
        onVerificationCompleted: (credential: PhoneAuthCredential) -> Unit,
        onError: (message: String) -> Unit
    )

    suspend fun verifyOtp(verificationId: String, otp: String): Result<User>
    suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<User>
    suspend fun loginOfficer(email: String, password: String): Result<User>
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    suspend fun updateFcmToken(token: String)
}

interface ComplaintRepository {
    fun getComplaints(): Flow<List<Complaint>>
    fun getUserComplaints(userId: String): Flow<List<Complaint>>
    fun getComplaintById(id: String): Flow<Complaint?>
    fun getComplaintsByStatus(status: ComplaintStatus): Flow<List<Complaint>>
    suspend fun submitComplaint(complaint: Complaint): Result<String>
    suspend fun updateComplaintStatus(
        complaintId: String,
        status: ComplaintStatus,
        officerNotes: String = "",
        afterImageUrl: String = ""
    ): Result<Unit>
    suspend fun uploadImage(imageBytes: ByteArray, path: String): Result<String>
    fun getNearbyComplaints(lat: Double, lng: Double, radiusKm: Double): Flow<List<Complaint>>
    suspend fun syncOfflineComplaints()
    fun getOfflineComplaints(): Flow<List<Complaint>>
}

interface NotificationRepository {
    fun getUserNotifications(userId: String): Flow<List<Notification>>
    suspend fun markNotificationRead(notificationId: String)
    suspend fun markAllNotificationsRead(userId: String)
    fun getUnreadCount(userId: String): Flow<Int>
}

interface UserRepository {
    suspend fun getUserById(id: String): User?
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun updateUserProfile(user: User): Result<Unit>
    fun getUserRole(): UserRole?
    suspend fun saveUserRole(role: UserRole)
    suspend fun getDashboardStats(userId: String?, role: UserRole): DashboardStats
}
