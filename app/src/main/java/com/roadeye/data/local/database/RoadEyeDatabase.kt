package com.roadeye.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.roadeye.data.local.dao.ComplaintDao
import com.roadeye.data.local.entity.ComplaintEntity

@Database(
    entities = [ComplaintEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RoadEyeDatabase : RoomDatabase() {
    abstract fun complaintDao(): ComplaintDao

    companion object {
        const val DATABASE_NAME = "roadeye_db"
    }
}
