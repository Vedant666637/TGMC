package com.tgm.tgmc.core.data.remote

import com.tgm.tgmc.core.domain.model.AuthToken
import com.tgm.tgmc.core.domain.model.ChildDevice
import com.tgm.tgmc.core.domain.model.AppInfo
import com.tgm.tgmc.core.domain.model.LocationData
import com.tgm.tgmc.core.domain.model.Geofence
import com.tgm.tgmc.core.domain.model.AlertItem
import com.tgm.tgmc.core.domain.model.TimeSchedule
import retrofit2.Response
import retrofit2.http.*

// ── Request/Response DTOs ─────────────────────────────────────────
data class LoginRequest(val email: String, val password: String)
data class GoogleLoginRequest(val idToken: String)
data class RegisterRequest(val email: String, val password: String, val displayName: String)
data class ForgotPasswordRequest(val email: String)
data class RefreshRequest(val refreshToken: String)
data class PairGenerateResponse(val code: String, val qrData: String, val expiresAt: Long)
data class PairActivateRequest(val code: String, val deviceName: String, val deviceModel: String)
data class PairActivateResponse(
    val deviceId: String,
    val parentEmail: String,
    val accessToken: String? = null,
    val refreshToken: String? = null
)

// ── Child API DTOs ──
data class EducationalContent(
    val id: String,
    val type: String,
    val title: String,
    val url: String,
    val thumbnailUrl: String?,
    val description: String?,
    val category: String?,
    val durationSecs: Int?,
    val expiresAt: String?,
    val publishedAt: String
)

data class StoreItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String
)

// ── API Interface ─────────────────────────────────────────────────
interface TgmcApiService {

    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthToken>

    @POST("api/auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<AuthToken>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthToken>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<AuthToken>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Unit>

    // Pairing
    @POST("api/pairing/generate")
    suspend fun generatePairingCode(): Response<PairGenerateResponse>

    @POST("api/pairing/activate")
    suspend fun activatePairingCode(@Body request: PairActivateRequest): Response<PairActivateResponse>

    // Devices
    @GET("api/devices")
    suspend fun getDevices(): Response<List<ChildDevice>>

    // App Block
    @GET("api/appblock/{deviceId}/rules")
    suspend fun getBlockRules(@Path("deviceId") deviceId: String): Response<List<AppInfo>>

    @POST("api/appblock/{deviceId}/rules")
    suspend fun setBlockRule(@Path("deviceId") deviceId: String, @Body app: AppInfo): Response<Unit>

    @DELETE("api/appblock/{deviceId}/rules/{packageName}")
    suspend fun removeBlockRule(
        @Path("deviceId") deviceId: String,
        @Path("packageName") packageName: String
    ): Response<Unit>

    // Schedule
    @GET("api/schedule/{deviceId}/rules")
    suspend fun getSchedules(@Path("deviceId") deviceId: String): Response<List<TimeSchedule>>

    @POST("api/schedule/{deviceId}/rules")
    suspend fun createSchedule(@Path("deviceId") deviceId: String, @Body schedule: TimeSchedule): Response<TimeSchedule>

    @DELETE("api/schedule/{deviceId}/rules/{scheduleId}")
    suspend fun deleteSchedule(
        @Path("deviceId") deviceId: String,
        @Path("scheduleId") scheduleId: String
    ): Response<Unit>

    // Location
    @GET("api/location/{deviceId}/history")
    suspend fun getLocationHistory(
        @Path("deviceId") deviceId: String,
        @Query("limit") limit: Int = 50
    ): Response<List<LocationData>>

    @POST("api/location/{deviceId}/ping")
    suspend fun sendLocationPing(@Path("deviceId") deviceId: String, @Body location: LocationData): Response<Unit>

    @GET("api/location/{deviceId}/geofences")
    suspend fun getGeofences(@Path("deviceId") deviceId: String): Response<List<Geofence>>

    @POST("api/location/{deviceId}/geofences")
    suspend fun createGeofence(@Path("deviceId") deviceId: String, @Body geofence: Geofence): Response<Geofence>

    // Alerts
    @GET("api/alerts/{deviceId}")
    suspend fun getAlerts(
        @Path("deviceId") deviceId: String,
        @Query("page") page: Int = 0
    ): Response<List<AlertItem>>

    @PUT("api/alerts/{alertId}/read")
    suspend fun markAlertRead(@Path("alertId") alertId: String): Response<Unit>

    @POST("api/alerts/{alertId}/resolve")
    suspend fun resolveAlert(@Path("alertId") alertId: String, @Body action: Map<String, String>): Response<Unit>

    // ── Phase 6: Child Content Hub ──
    @GET("api/child/content")
    suspend fun getEducationalContent(): Response<List<EducationalContent>>

    @GET("api/child/content/statuses")
    suspend fun getStatuses(): Response<List<EducationalContent>>

    @GET("api/child/content/feed")
    suspend fun getFeed(): Response<List<EducationalContent>>

    @GET("api/child/store")
    suspend fun getStoreItems(): Response<List<StoreItem>>

    // ── Web Content Filtering ──
    @GET("api/webfilter/{deviceId}/rules")
    suspend fun getWebFilterRules(@Path("deviceId") deviceId: String): Response<List<WebFilterRuleDto>>

    @POST("api/webfilter/{deviceId}/rules")
    suspend fun addWebFilterRule(@Path("deviceId") deviceId: String, @Body rule: Map<String, @JvmSuppressWildcards Any>): Response<WebFilterRuleDto>

    @DELETE("api/webfilter/{deviceId}/rules/{ruleId}")
    suspend fun deleteWebFilterRule(@Path("deviceId") deviceId: String, @Path("ruleId") ruleId: String): Response<Unit>

    @POST("api/webfilter/{deviceId}/seed-defaults")
    suspend fun seedWebFilterDefaults(@Path("deviceId") deviceId: String): Response<Unit>

    // ── Phase 2: SOS Functionality ──
    @POST("api/child/{deviceId}/sos")
    suspend fun triggerSos(
        @Path("deviceId") deviceId: String, 
        @Body location: LocationData? = null
    ): Response<Unit>

    // ── Phase 6: Purchase Request ──
    @POST("api/child/{deviceId}/purchase-request/{itemId}")
    suspend fun requestPurchase(
        @Path("deviceId") deviceId: String,
        @Path("itemId") itemId: String
    ): Response<Unit>
    // ── FCM & Commands ──
    @POST("api/commands/{deviceId}/fcm-token")
    suspend fun updateFcmToken(
        @Path("deviceId") deviceId: String,
        @Body request: Map<String, String>
    ): Response<Unit>

    @POST("api/commands/{deviceId}/heartbeat")
    suspend fun sendHeartbeat(
        @Path("deviceId") deviceId: String
    ): Response<Unit>

    @POST("api/commands/{deviceId}/command")
    suspend fun sendCommand(
        @Path("deviceId") deviceId: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>

    @POST("api/commands/{deviceId}/push-rules")
    suspend fun pushRules(
        @Path("deviceId") deviceId: String
    ): Response<Unit>

    // ── Message Logging (Phase 7) ──
    @POST("api/messages/{deviceId}")
    suspend fun logMessage(
        @Path("deviceId") deviceId: String,
        @Body request: Map<String, @JvmSuppressWildcards Any>
    ): Response<Unit>
}

// ── Web Filter DTO ──
data class WebFilterRuleDto(
    val id: String,
    val ruleType: String,
    val value: String,
    val isBlocked: Boolean
)
