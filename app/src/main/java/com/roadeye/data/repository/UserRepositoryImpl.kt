package com.roadeye.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.firestore.FirebaseFirestore
import com.roadeye.domain.model.*
import com.roadeye.domain.repository.NotificationRepository
import com.roadeye.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "roadeye_prefs")

@Singleton
class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore
) : UserRepository {

    companion object {
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
    }

    override suspend fun getUserById(id: String): User? {
        return try {
            val doc = firestore.collection("users").document(id).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) { null }
    }

    override suspend fun saveUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override fun getUserRole(): UserRole? {
        // Synchronous read — use with caution (only at startup)
        return null // Will be read via Flow
    }

    override suspend fun saveUserRole(role: UserRole) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ROLE] = role.name
        }
    }

    override suspend fun getDashboardStats(userId: String?, role: UserRole): DashboardStats {
        return try {
            val query = if (role == UserRole.CITIZEN && userId != null) {
                firestore.collection("complaints").whereEqualTo("userId", userId)
            } else {
                firestore.collection("complaints")
            }
            val docs = query.get().await()
            val complaints = docs.documents.mapNotNull { it.toObject(FirestoreComplaint::class.java) }

            DashboardStats(
                totalComplaints = complaints.size,
                submittedCount = complaints.count { it.status == ComplaintStatus.SUBMITTED.name },
                inProgressCount = complaints.count { it.status == ComplaintStatus.IN_PROGRESS.name },
                resolvedCount = complaints.count { it.status == ComplaintStatus.RESOLVED.name },
                highSeverityCount = complaints.count { it.severity == ComplaintSeverity.HIGH.name }
            )
        } catch (e: Exception) {
            DashboardStats()
        }
    }
}

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override fun getUserNotifications(userId: String): Flow<List<Notification>> = flow {
        try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .get().await()
            emit(snapshot.documents.mapNotNull { it.toObject(Notification::class.java) })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun markNotificationRead(notificationId: String) {
        try {
            firestore.collection("notifications").document(notificationId)
                .update("isRead", true).await()
        } catch (e: Exception) { }
    }

    override suspend fun markAllNotificationsRead(userId: String) {
        try {
            val batch = firestore.batch()
            val docs = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get().await()
            docs.documents.forEach { batch.update(it.reference, "isRead", true) }
            batch.commit().await()
        } catch (e: Exception) { }
    }

    override fun getUnreadCount(userId: String): Flow<Int> = flow {
        try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get().await()
            emit(snapshot.size())
        } catch (e: Exception) { emit(0) }
    }
}
