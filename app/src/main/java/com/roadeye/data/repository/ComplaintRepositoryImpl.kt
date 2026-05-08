package com.roadeye.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.roadeye.data.local.dao.ComplaintDao
import com.roadeye.data.local.entity.ComplaintEntity
import com.roadeye.data.local.entity.toDomain
import com.roadeye.data.local.entity.toEntity
import com.roadeye.domain.model.*
import com.roadeye.domain.repository.ComplaintRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class ComplaintRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val complaintDao: ComplaintDao
) : ComplaintRepository {

    companion object {
        private const val TAG = "ComplaintRepo"
        private const val COLLECTION = "complaints"
    }

    override fun getComplaints(): Flow<List<Complaint>> = flow {
        try {
            val snapshot = firestore.collection(COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            val complaints = snapshot.documents.mapNotNull { doc ->
                doc.toObject(FirestoreComplaint::class.java)?.toDomain(doc.id)
            }
            // Cache locally
            complaintDao.insertComplaints(complaints.map { it.toEntity() })
            emit(complaints)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching complaints, using cache", e)
            // Fallback to local cache
            emitAll(complaintDao.getAllComplaints().map { list -> list.map { it.toDomain() } })
        }
    }

    override fun getUserComplaints(userId: String): Flow<List<Complaint>> = flow {
        try {
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            val complaints = snapshot.documents.mapNotNull { doc ->
                doc.toObject(FirestoreComplaint::class.java)?.toDomain(doc.id)
            }
            complaintDao.insertComplaints(complaints.map { it.toEntity() })
            emit(complaints)
        } catch (e: Exception) {
            emitAll(complaintDao.getUserComplaints(userId).map { list -> list.map { it.toDomain() } })
        }
    }

    override fun getComplaintById(id: String): Flow<Complaint?> = flow {
        try {
            val doc = firestore.collection(COLLECTION).document(id).get().await()
            val complaint = doc.toObject(FirestoreComplaint::class.java)?.toDomain(doc.id)
            emit(complaint)
        } catch (e: Exception) {
            emitAll(complaintDao.getComplaintById(id).map { it?.toDomain() })
        }
    }

    override fun getComplaintsByStatus(status: ComplaintStatus): Flow<List<Complaint>> = flow {
        try {
            val snapshot = firestore.collection(COLLECTION)
                .whereEqualTo("status", status.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            emit(snapshot.documents.mapNotNull { doc ->
                doc.toObject(FirestoreComplaint::class.java)?.toDomain(doc.id)
            })
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun submitComplaint(complaint: Complaint): Result<String> {
        return try {
            val id = if (complaint.id.isEmpty()) UUID.randomUUID().toString() else complaint.id
            val firestoreComplaint = FirestoreComplaint.fromDomain(complaint.copy(id = id))
            firestore.collection(COLLECTION).document(id).set(firestoreComplaint).await()
            // Cache locally as synced
            complaintDao.insertComplaint(complaint.copy(id = id, isSynced = true).toEntity())
            Result.success(id)
        } catch (e: Exception) {
            // Save offline
            val id = UUID.randomUUID().toString()
            complaintDao.insertComplaint(
                complaint.copy(id = id, isSynced = false).toEntity()
            )
            Log.e(TAG, "Saved offline: $id", e)
            Result.success(id)
        }
    }

    override suspend fun updateComplaintStatus(
        complaintId: String,
        status: ComplaintStatus,
        officerNotes: String,
        afterImageUrl: String
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status.name,
                "updatedAt" to System.currentTimeMillis()
            )
            if (officerNotes.isNotEmpty()) updates["officerNotes"] = officerNotes
            if (afterImageUrl.isNotEmpty()) updates["afterImageUrl"] = afterImageUrl
            if (status == ComplaintStatus.RESOLVED) updates["resolvedAt"] = System.currentTimeMillis()

            firestore.collection(COLLECTION).document(complaintId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadImage(imageBytes: ByteArray, path: String): Result<String> {
        return try {
            val ref = storage.reference.child(path)
            ref.putBytes(imageBytes).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getNearbyComplaints(lat: Double, lng: Double, radiusKm: Double): Flow<List<Complaint>> {
        val delta = radiusKm / 111.0
        return complaintDao.getNearbyComplaints(
            lat - delta, lat + delta,
            lng - delta, lng + delta
        ).map { list ->
            list.map { it.toDomain() }.filter { complaint ->
                haversineDistance(lat, lng, complaint.latitude, complaint.longitude) <= radiusKm
            }
        }
    }

    override suspend fun syncOfflineComplaints() {
        try {
            val unsyncedFlow = complaintDao.getUnsyncedComplaints()
            unsyncedFlow.first().forEach { entity ->
                val complaint = entity.toDomain()
                val firestoreComplaint = FirestoreComplaint.fromDomain(complaint)
                firestore.collection(COLLECTION).document(entity.id).set(firestoreComplaint).await()
                complaintDao.markAsSynced(entity.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
        }
    }

    override fun getOfflineComplaints(): Flow<List<Complaint>> =
        complaintDao.getUnsyncedComplaints().map { list -> list.map { it.toDomain() } }

    private fun haversineDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}

// Firestore data class for serialization
data class FirestoreComplaint(
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val beforeImageUrl: String = "",
    val afterImageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val district: String = "",
    val ward: String = "",
    val severity: String = "",
    val status: String = "",
    val assignedOfficerId: String = "",
    val assignedOfficerName: String = "",
    val officerNotes: String = "",
    val roadHealthScore: Int = 0,
    val resolvedAt: Long? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toDomain(id: String) = Complaint(
        id = id,
        userId = userId,
        userName = userName,
        userPhone = userPhone,
        title = title,
        description = description,
        imageUrl = imageUrl,
        beforeImageUrl = beforeImageUrl,
        afterImageUrl = afterImageUrl,
        latitude = latitude,
        longitude = longitude,
        address = address,
        district = district,
        ward = ward,
        severity = runCatching { ComplaintSeverity.valueOf(severity) }.getOrDefault(ComplaintSeverity.MEDIUM),
        status = runCatching { ComplaintStatus.valueOf(status) }.getOrDefault(ComplaintStatus.SUBMITTED),
        assignedOfficerId = assignedOfficerId,
        assignedOfficerName = assignedOfficerName,
        officerNotes = officerNotes,
        roadHealthScore = roadHealthScore,
        resolvedAt = resolvedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(c: Complaint) = FirestoreComplaint(
            userId = c.userId,
            userName = c.userName,
            userPhone = c.userPhone,
            title = c.title,
            description = c.description,
            imageUrl = c.imageUrl,
            beforeImageUrl = c.beforeImageUrl,
            afterImageUrl = c.afterImageUrl,
            latitude = c.latitude,
            longitude = c.longitude,
            address = c.address,
            district = c.district,
            ward = c.ward,
            severity = c.severity.name,
            status = c.status.name,
            assignedOfficerId = c.assignedOfficerId,
            assignedOfficerName = c.assignedOfficerName,
            officerNotes = c.officerNotes,
            roadHealthScore = c.roadHealthScore,
            resolvedAt = c.resolvedAt,
            createdAt = c.createdAt,
            updatedAt = c.updatedAt
        )
    }
}
