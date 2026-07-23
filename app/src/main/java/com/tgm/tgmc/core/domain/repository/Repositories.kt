package com.tgm.tgmc.core.domain.repository

import com.tgm.tgmc.core.domain.model.ChildDevice
import com.tgm.tgmc.core.domain.model.AppInfo
import com.tgm.tgmc.core.domain.model.AlertItem
import com.tgm.tgmc.core.domain.model.LocationData
import com.tgm.tgmc.core.domain.model.Geofence
import com.tgm.tgmc.core.domain.model.TimeSchedule
import com.tgm.tgmc.core.util.Result
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    suspend fun getDevices(): Result<List<ChildDevice>>
    suspend fun unpairDevice(deviceId: String): Result<Unit>
}

interface AppBlockRepository {
    suspend fun getBlockRules(deviceId: String): Result<List<AppInfo>>
    suspend fun setBlockRule(deviceId: String, app: AppInfo): Result<Unit>
    suspend fun removeBlockRule(deviceId: String, packageName: String): Result<Unit>
}

interface ScheduleRepository {
    suspend fun getSchedules(deviceId: String): Result<List<TimeSchedule>>
    suspend fun createSchedule(deviceId: String, schedule: TimeSchedule): Result<TimeSchedule>
    suspend fun deleteSchedule(deviceId: String, scheduleId: String): Result<Unit>
}

interface LocationRepository {
    suspend fun getLocationHistory(deviceId: String, limit: Int = 50): Result<List<LocationData>>
    suspend fun sendLocationPing(deviceId: String, location: LocationData): Result<Unit>
    suspend fun getGeofences(deviceId: String): Result<List<Geofence>>
    suspend fun createGeofence(deviceId: String, geofence: Geofence): Result<Geofence>
    suspend fun deleteGeofence(deviceId: String, geofenceId: String): Result<Unit>
}

interface AlertRepository {
    suspend fun getAlerts(deviceId: String, page: Int = 0): Result<List<AlertItem>>
    suspend fun markAlertRead(alertId: String): Result<Unit>
}

interface PairingRepository {
    suspend fun generatePairingCode(): Result<Triple<String, String, Long>>  // code, qrData, expiresAt
    suspend fun activateCode(code: String, deviceModel: String, deviceName: String?): Result<Pair<String, String>>  // deviceId, parentEmail
}
