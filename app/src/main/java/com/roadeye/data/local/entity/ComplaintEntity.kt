package com.roadeye.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.roadeye.domain.model.ComplaintSeverity
import com.roadeye.domain.model.ComplaintStatus

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val userName: String,
    val userPhone: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val localImagePath: String = "",
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val district: String,
    val severity: String, // ComplaintSeverity.name
    val status: String,   // ComplaintStatus.name
    val isSynced: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

fun ComplaintEntity.toDomain() = com.roadeye.domain.model.Complaint(
    id = id,
    userId = userId,
    userName = userName,
    userPhone = userPhone,
    title = title,
    description = description,
    imageUrl = imageUrl,
    latitude = latitude,
    longitude = longitude,
    address = address,
    district = district,
    severity = ComplaintSeverity.valueOf(severity),
    status = ComplaintStatus.valueOf(status),
    isSynced = isSynced,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun com.roadeye.domain.model.Complaint.toEntity() = ComplaintEntity(
    id = id,
    userId = userId,
    userName = userName,
    userPhone = userPhone,
    title = title,
    description = description,
    imageUrl = imageUrl,
    latitude = latitude,
    longitude = longitude,
    address = address,
    district = district,
    severity = severity.name,
    status = status.name,
    isSynced = isSynced,
    createdAt = createdAt,
    updatedAt = updatedAt
)
