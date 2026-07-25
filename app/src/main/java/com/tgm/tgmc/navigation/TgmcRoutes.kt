package com.tgm.tgmc.navigation

/**
 * All navigation routes in the TGM-C app, organized by graph.
 */
object TgmcRoutes {

    // ── Auth Graph ──────────────────────────────────────────────
    object Auth {
        const val LOGIN           = "auth/login"
        const val REGISTER        = "auth/register"
        const val FORGOT_PASSWORD = "auth/forgot_password"
        const val RESET_PASSWORD  = "auth/reset_password"
        const val ROLE_SELECTION  = "auth/role_selection"
    }

    // ── Parent Graph ─────────────────────────────────────────────
    object Parent {
        const val MAIN_LAYOUT = "parent/main_layout"
        const val FEED       = "parent/feed"
        const val STORE      = "parent/store"
        const val MESSAGES   = "parent/messages"
        const val DASHBOARD  = "parent/dashboard" // The Child Controls tab
        
        const val APP_BLOCK  = "parent/app_block"
        const val SCHEDULE   = "parent/schedule"
        const val LOCATION   = "parent/location"
        const val CAMERA     = "parent/camera"
        const val MIRROR     = "parent/screen_mirror"
        const val AUDIO      = "parent/live_audio"
        const val ALERTS     = "parent/alerts"
        const val WEB_FILTER = "parent/web_filter"
        const val ACTIVITY_REPORT = "parent/activity_report"
        const val SETTINGS   = "parent/settings"
        // Pairing
        const val PAIRING_START = "parent/pairing/start"
        const val PAIRING_QR    = "parent/pairing/qr"
    }

    // ── Child Graph ──────────────────────────────────────────────
    object Child {
        const val PAIR    = "child/pair"
        const val CONSENT = "child/consent"
        const val MAIN_LAYOUT = "child/main_layout"
        const val FEED    = "child/feed"
        const val STORE   = "child/store"
        const val MESSAGES = "child/messages"
    }

    // ── Top-level graphs ─────────────────────────────────────────
    const val AUTH_GRAPH   = "graph/auth"
    const val PARENT_GRAPH = "graph/parent"
    const val CHILD_GRAPH  = "graph/child"
    const val SPLASH       = "splash"
}
