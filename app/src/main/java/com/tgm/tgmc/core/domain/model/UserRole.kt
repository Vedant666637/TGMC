package com.tgm.tgmc.core.domain.model

/**
 * Represents the role of the current app user.
 * Written to DataStore at login/pairing; used for nav graph selection.
 */
enum class UserRole {
    PARENT, CHILD, NONE
}
