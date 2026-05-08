package com.roadeye.data.local.dao

import androidx.room.*
import com.roadeye.data.local.entity.ComplaintEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserComplaints(userId: String): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE isSynced = 0")
    fun getUnsyncedComplaints(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE id = :id")
    fun getComplaintById(id: String): Flow<ComplaintEntity?>

    @Query("""
        SELECT * FROM complaints 
        WHERE (latitude BETWEEN :minLat AND :maxLat) 
        AND (longitude BETWEEN :minLng AND :maxLng)
    """)
    fun getNearbyComplaints(
        minLat: Double, maxLat: Double,
        minLng: Double, maxLng: Double
    ): Flow<List<ComplaintEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaints(complaints: List<ComplaintEntity>)

    @Update
    suspend fun updateComplaint(complaint: ComplaintEntity)

    @Query("UPDATE complaints SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Delete
    suspend fun deleteComplaint(complaint: ComplaintEntity)

    @Query("SELECT COUNT(*) FROM complaints WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>
}
