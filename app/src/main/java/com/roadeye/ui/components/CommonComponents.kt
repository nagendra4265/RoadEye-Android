package com.roadeye.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.roadeye.domain.model.*
import com.roadeye.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────
// Complaint Card
// ─────────────────────────────────────────────

@Composable
fun ComplaintCard(
    complaint: Complaint,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SeverityBadge(severity = complaint.severity)
                        Spacer(Modifier.width(8.dp))
                        StatusBadge(status = complaint.status)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = complaint.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = complaint.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (complaint.imageUrl.isNotEmpty()) {
                    Spacer(Modifier.width(12.dp))
                    AsyncImage(
                        model = complaint.imageUrl,
                        contentDescription = "Complaint image",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛣️", fontSize = 28.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(0.1f))
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = complaint.address.ifEmpty { "Location captured" },
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.outline
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 180.dp)
                    )
                }

                Text(
                    text = formatDate(complaint.createdAt),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // Offline indicator
            if (!complaint.isSynced) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Saved offline – will sync when connected",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Severity Badge
// ─────────────────────────────────────────────

@Composable
fun SeverityBadge(severity: ComplaintSeverity) {
    val color = when (severity) {
        ComplaintSeverity.HIGH -> SeverityHigh
        ComplaintSeverity.MEDIUM -> SeverityMedium
        ComplaintSeverity.LOW -> SeverityLow
    }

    Surface(
        color = color.copy(0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = severity.displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = color,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

// ─────────────────────────────────────────────
// Status Badge
// ─────────────────────────────────────────────

@Composable
fun StatusBadge(status: ComplaintStatus) {
    val (color, icon) = when (status) {
        ComplaintStatus.SUBMITTED -> Pair(StatusSubmitted, "📤")
        ComplaintStatus.IN_PROGRESS -> Pair(StatusInProgress, "🔧")
        ComplaintStatus.RESOLVED -> Pair(StatusResolved, "✅")
    }

    Surface(
        color = color.copy(0.1f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 10.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                text = status.displayName,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

// ─────────────────────────────────────────────
// Stat Card
// ─────────────────────────────────────────────

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = color,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            )
        }
    }
}

// ─────────────────────────────────────────────
// Road Health Meter
// ─────────────────────────────────────────────

@Composable
fun RoadHealthMeter(
    modifier: Modifier = Modifier,
    score: Int,
    district: String
) {
    val healthColor = when {
        score >= 70 -> HealthGood
        score >= 40 -> HealthMedium
        else -> HealthDangerous
    }
    val healthLabel = when {
        score >= 70 -> "Good • మంచి"
        score >= 40 -> "Moderate • మధ్యస్థం"
        else -> "Dangerous • ప్రమాదకర"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Road Health Index",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "$district District",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                    )
                }
                Surface(
                    color = healthColor.copy(0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        healthLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = healthColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                    Text("$score/100", style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold, color = healthColor
                    ))
                    Text("100", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline))
                }
                Spacer(Modifier.height(6.dp))

                val animatedProgress by animateFloatAsState(
                    targetValue = score / 100f,
                    animationSpec = tween(1200, easing = EaseOutBounce),
                    label = "health_progress"
                )

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                    color = healthColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            }

            Spacer(Modifier.height(12.dp))

            // Color Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem("● Good", HealthGood)
                LegendItem("● Moderate", HealthMedium)
                LegendItem("● Danger", HealthDangerous)
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Text(
        label,
        style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.Medium)
    )
}

// ─────────────────────────────────────────────
// Helper
// ─────────────────────────────────────────────

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
