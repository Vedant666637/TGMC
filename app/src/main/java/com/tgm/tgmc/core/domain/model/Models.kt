package com.tgm.tgmc.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val role: UserRole,
    val userId: String,
    val email: String
)

@Serializable
data class ChildDevice(
    val deviceId: String,
    val childName: String,
    val model: String,
    val isOnline: Boolean = false,         // default false; real-time status comes from Firebase
    val batteryLevel: Int = 0,             // optional field, default 0
    val pairedAt: String = ""              // Prisma returns ISO-8601 string e.g. "2026-08-28T13:20:34.110Z"
)

@Serializable
data class AppInfo(
    val packageName: String,
    val appName: String,
    val iconUrl: String? = null,
    val category: AppCategory,
    val isBlocked: Boolean = false
)

enum class AppCategory {
    SOCIAL, GAMES, STREAMING, EDUCATION, PRODUCTIVITY, COMMUNICATION, OTHER
}

@Serializable
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long,
    val address: String? = null
)

@Serializable
data class Geofence(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val alertOnEnter: Boolean = true,
    val alertOnExit: Boolean = true
)

@Serializable
data class AlertItem(
    val id: String,
    val type: AlertType,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val deviceId: String
)

enum class AlertType {
    APP_INSTALLED, BLOCKED_KEYWORD, GEOFENCE_ENTER, GEOFENCE_EXIT,
    LOW_BATTERY, SOS, SCREEN_TIME_LIMIT
}

@Serializable
data class TimeSchedule(
    val id: String,
    val name: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val activeDays: Set<Int>, // 0=Sun, 1=Mon … 6=Sat
    val isEnabled: Boolean = true
)
