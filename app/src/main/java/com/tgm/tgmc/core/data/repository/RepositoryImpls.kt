package com.tgm.tgmc.core.data.repository

import com.tgm.tgmc.core.data.remote.TgmcApiService
import com.tgm.tgmc.core.data.remote.PairActivateRequest
import com.tgm.tgmc.core.data.local.TgmcDataStore
import com.tgm.tgmc.core.domain.model.*
import com.tgm.tgmc.core.domain.repository.*
import com.tgm.tgmc.core.util.Result
import javax.inject.Inject
import javax.inject.Singleton

// ── Device Repository ─────────────────────────────────────────────
@Singleton
class DeviceRepositoryImpl @Inject constructor(private val api: TgmcApiService) : DeviceRepository {
    override suspend fun getDevices(): Result<List<ChildDevice>> = safeCall { api.getDevices().body()!! }
    override suspend fun unpairDevice(deviceId: String): Result<Unit> = safeCall { Unit }
}

// ── AppBlock Repository ───────────────────────────────────────────
@Singleton
class AppBlockRepositoryImpl @Inject constructor(private val api: TgmcApiService) : AppBlockRepository {
    override suspend fun getBlockRules(deviceId: String): Result<List<AppInfo>> =
        safeCall { api.getBlockRules(deviceId).body()!! }

    override suspend fun setBlockRule(deviceId: String, app: AppInfo): Result<Unit> =
        safeCall { api.setBlockRule(deviceId, app); Unit }

    override suspend fun removeBlockRule(deviceId: String, packageName: String): Result<Unit> =
        safeCall { api.removeBlockRule(deviceId, packageName); Unit }
}

// ── Schedule Repository ───────────────────────────────────────────
@Singleton
class ScheduleRepositoryImpl @Inject constructor(private val api: TgmcApiService) : ScheduleRepository {
    override suspend fun getSchedules(deviceId: String): Result<List<TimeSchedule>> =
        safeCall { api.getSchedules(deviceId).body()!! }

    override suspend fun createSchedule(deviceId: String, schedule: TimeSchedule): Result<TimeSchedule> =
        safeCall { api.createSchedule(deviceId, schedule).body()!! }

    override suspend fun deleteSchedule(deviceId: String, scheduleId: String): Result<Unit> =
        safeCall { api.deleteSchedule(deviceId, scheduleId); Unit }
}

// ── Location Repository ───────────────────────────────────────────
@Singleton
class LocationRepositoryImpl @Inject constructor(private val api: TgmcApiService) : LocationRepository {
    override suspend fun getLocationHistory(deviceId: String, limit: Int): Result<List<LocationData>> =
        safeCall { api.getLocationHistory(deviceId, limit).body()!! }

    override suspend fun sendLocationPing(deviceId: String, location: LocationData): Result<Unit> =
        safeCall { api.sendLocationPing(deviceId, location); Unit }

    override suspend fun getGeofences(deviceId: String): Result<List<Geofence>> =
        safeCall { api.getGeofences(deviceId).body()!! }

    override suspend fun createGeofence(deviceId: String, geofence: Geofence): Result<Geofence> =
        safeCall { api.createGeofence(deviceId, geofence).body()!! }

    override suspend fun deleteGeofence(deviceId: String, geofenceId: String): Result<Unit> =
        safeCall { Unit } // add DELETE endpoint later
}

// ── Alert Repository ──────────────────────────────────────────────
@Singleton
class AlertRepositoryImpl @Inject constructor(private val api: TgmcApiService) : AlertRepository {
    override suspend fun getAlerts(deviceId: String, page: Int): Result<List<AlertItem>> =
        safeCall { api.getAlerts(deviceId, page).body()!! }

    override suspend fun markAlertRead(alertId: String): Result<Unit> =
        safeCall { api.markAlertRead(alertId); Unit }
}

// ── Pairing Repository ────────────────────────────────────────────
@Singleton
class PairingRepositoryImpl @Inject constructor(
    private val api: TgmcApiService,
    private val dataStore: TgmcDataStore
) : PairingRepository {
    override suspend fun generatePairingCode(): Result<Triple<String, String, Long>> =
        safeCall {
            val body = api.generatePairingCode().body()!!
            Triple(body.code, body.qrData, body.expiresAt)
        }

    override suspend fun activateCode(code: String, deviceModel: String, deviceName: String?): Result<Pair<String, String>> =
        safeCall {
            val body = api.activatePairingCode(
                PairActivateRequest(code, deviceName ?: "Child", deviceModel)
            ).body()!!
            dataStore.saveDeviceId(body.deviceId)
            dataStore.markPaired(true)
            Pair(body.deviceId, body.parentEmail)
        }
}

// ── Safe call wrapper ─────────────────────────────────────────────
private inline fun <T> safeCall(block: () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(e.message ?: "An unexpected error occurred")
    }
}
