package com.roadeye.domain.model

import com.google.firebase.Timestamp

// ─────────────────────────────────────────────
// Core Domain Models
// ─────────────────────────────────────────────

enum class UserRole {
    CITIZEN, OFFICER
}

enum class ComplaintStatus(val displayName: String, val teluguName: String) {
    SUBMITTED("Submitted", "సమర్పించారు"),
    IN_PROGRESS("In Progress", "పనిలో ఉంది"),
    RESOLVED("Resolved", "పరిష్కరించబడింది")
}

enum class ComplaintSeverity(val displayName: String, val teluguName: String, val level: Int) {
    LOW("Low", "తక్కువ", 1),
    MEDIUM("Medium", "మధ్యస్థం", 2),
    HIGH("High", "అధికం", 3)
}

data class User(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val role: UserRole = UserRole.CITIZEN,
    val district: String = "",
    val ward: String = "",
    val profileImageUrl: String = "",
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Complaint(
    val id: String = "",
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
    val severity: ComplaintSeverity = ComplaintSeverity.MEDIUM,
    val status: ComplaintStatus = ComplaintStatus.SUBMITTED,
    val assignedOfficerId: String = "",
    val assignedOfficerName: String = "",
    val officerNotes: String = "",
    val roadHealthScore: Int = 0, // 0-100
    val estimatedRepairDate: Long? = null,
    val resolvedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = true,
    val isDuplicate: Boolean = false
)

data class Notification(
    val id: String = "",
    val userId: String = "",
    val complaintId: String = "",
    val title: String = "",
    val body: String = "",
    val type: NotificationType = NotificationType.STATUS_UPDATE,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class NotificationType {
    STATUS_UPDATE,
    NEW_COMPLAINT,
    OFFICER_ASSIGNED,
    RESOLVED
}

data class RoadHealthStats(
    val district: String = "",
    val totalComplaints: Int = 0,
    val resolvedComplaints: Int = 0,
    val pendingComplaints: Int = 0,
    val highSeverityCount: Int = 0,
    val healthScore: Int = 0, // 0-100
    val healthLabel: String = "Good"
)

data class DashboardStats(
    val totalComplaints: Int = 0,
    val submittedCount: Int = 0,
    val inProgressCount: Int = 0,
    val resolvedCount: Int = 0,
    val highSeverityCount: Int = 0,
    val averageResolutionDays: Double = 0.0
)
