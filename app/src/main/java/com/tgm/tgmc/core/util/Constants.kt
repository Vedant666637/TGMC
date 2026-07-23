package com.tgm.tgmc.core.util

object Constants {
    // DataStore keys
    const val PREF_KEY_ACCESS_TOKEN  = "access_token"
    const val PREF_KEY_REFRESH_TOKEN = "refresh_token"
    const val PREF_KEY_USER_ROLE     = "user_role"
    const val PREF_KEY_USER_ID       = "user_id"
    const val PREF_KEY_USER_EMAIL    = "user_email"
    const val PREF_KEY_IS_PAIRED     = "is_paired"
    const val PREF_KEY_DEVICE_ID     = "device_id"
    const val PREF_KEY_CONSENT_GIVEN = "consent_given"

    // Notification channels
    const val CHANNEL_MONITORING      = "tgmc_monitoring"
    const val CHANNEL_ALERTS          = "tgmc_alerts"
    const val CHANNEL_SOS             = "tgmc_sos"

    // Notification IDs
    const val NOTIF_ID_FOREGROUND     = 1001
    const val NOTIF_ID_SOS            = 1002
    const val NOTIF_ID_ALERT          = 1003

    // Foreground service action intents
    const val ACTION_START_MONITORING = "com.tgm.tgmc.START_MONITORING"
    const val ACTION_STOP_MONITORING  = "com.tgm.tgmc.STOP_MONITORING"

    // WebSocket events
    const val WS_DEVICE_JOIN         = "device:join"
    const val WS_DEVICE_LOCATION     = "device:location"
    const val WS_CAMERA_REQUEST      = "camera:request"
    const val WS_CAMERA_FRAME        = "camera:frame"
    const val WS_MIRROR_START        = "mirror:start"
    const val WS_MIRROR_FRAME        = "mirror:frame"
    const val WS_AUDIO_START         = "audio:start"
    const val WS_AUDIO_CHUNK         = "audio:chunk"
    const val WS_SOS_TRIGGER         = "sos:trigger"
    const val WS_RULE_UPDATE         = "rule:update"

    // Intervals
    const val LOCATION_UPDATE_INTERVAL_MS = 15_000L // 15s
    const val RULE_SYNC_INTERVAL_HOURS    = 1L
}
