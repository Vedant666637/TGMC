package com.tgm.tgmc.core.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.tgm.tgmc.core.domain.model.UserRole
import com.tgm.tgmc.core.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tgmc_prefs")

@Singleton
class TgmcDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // ── Keys ──────────────────────────────────────────────────────
    private val ACCESS_TOKEN  = stringPreferencesKey(Constants.PREF_KEY_ACCESS_TOKEN)
    private val REFRESH_TOKEN = stringPreferencesKey(Constants.PREF_KEY_REFRESH_TOKEN)
    private val USER_ROLE     = stringPreferencesKey(Constants.PREF_KEY_USER_ROLE)
    private val USER_ID       = stringPreferencesKey(Constants.PREF_KEY_USER_ID)
    private val USER_EMAIL    = stringPreferencesKey(Constants.PREF_KEY_USER_EMAIL)
    private val IS_PAIRED     = booleanPreferencesKey(Constants.PREF_KEY_IS_PAIRED)
    private val DEVICE_ID     = stringPreferencesKey(Constants.PREF_KEY_DEVICE_ID)
    private val CONSENT_GIVEN = booleanPreferencesKey(Constants.PREF_KEY_CONSENT_GIVEN)
    private val BLOCKED_PACKAGES = stringSetPreferencesKey("blocked_packages")
    private val BLOCKED_DOMAINS = stringSetPreferencesKey("blocked_domains")
    private val BLOCKED_KEYWORDS = stringSetPreferencesKey("blocked_keywords")
    private val SCHEDULES = stringPreferencesKey("schedules")
    private val SELECTED_DEVICE_ID = stringPreferencesKey("selected_device_id")

    // ── Reads ─────────────────────────────────────────────────────
    val blockedPackages: Flow<Set<String>> = dataStore.data.map { it[BLOCKED_PACKAGES] ?: emptySet() }
    val blockedDomains: Flow<Set<String>> = dataStore.data.map { it[BLOCKED_DOMAINS] ?: emptySet() }
    val blockedKeywords: Flow<Set<String>> = dataStore.data.map { it[BLOCKED_KEYWORDS] ?: emptySet() }
    val schedules: Flow<String> = dataStore.data.map { it[SCHEDULES] ?: "[]" }
    val accessToken: Flow<String?> = dataStore.data.map {
        val raw = it[ACCESS_TOKEN]
        if (!raw.isNullOrEmpty()) com.tgm.tgmc.core.util.CryptoManager.decrypt(raw) else null
    }
    val refreshToken: Flow<String?> = dataStore.data.map {
        val raw = it[REFRESH_TOKEN]
        if (!raw.isNullOrEmpty()) com.tgm.tgmc.core.util.CryptoManager.decrypt(raw) else null
    }
    val userRole: Flow<UserRole> = dataStore.data.map {
        when (it[USER_ROLE]) {
            "PARENT" -> UserRole.PARENT
            "CHILD"  -> UserRole.CHILD
            else     -> UserRole.NONE
        }
    }
    val userId: Flow<String?> = dataStore.data.map { it[USER_ID] }
    val userEmail: Flow<String?> = dataStore.data.map { it[USER_EMAIL] }
    val isPaired: Flow<Boolean> = dataStore.data.map { it[IS_PAIRED] ?: false }
    val deviceId: Flow<String?> = dataStore.data.map { it[DEVICE_ID] }
    val consentGiven: Flow<Boolean> = dataStore.data.map { it[CONSENT_GIVEN] ?: false }
    // The parent's currently selected child device (survives screen navigation)
    val selectedDeviceId: Flow<String?> = dataStore.data.map { it[SELECTED_DEVICE_ID] }

    // ── Writes ────────────────────────────────────────────────────
    suspend fun saveAuthTokens(accessToken: String, refreshToken: String, role: UserRole, userId: String, email: String) {
        dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN]  = com.tgm.tgmc.core.util.CryptoManager.encrypt(accessToken)
            prefs[REFRESH_TOKEN] = com.tgm.tgmc.core.util.CryptoManager.encrypt(refreshToken)
            prefs[USER_ROLE]     = role.name
            prefs[USER_ID]       = userId
            prefs[USER_EMAIL]    = email
        }
    }


    suspend fun saveDeviceId(deviceId: String) {
        dataStore.edit { it[DEVICE_ID] = deviceId }
    }

    suspend fun saveSelectedDeviceId(deviceId: String) {
        dataStore.edit { it[SELECTED_DEVICE_ID] = deviceId }
    }

    suspend fun markPaired(isPaired: Boolean) {
        dataStore.edit { it[IS_PAIRED] = isPaired }
    }

    suspend fun saveBlockedPackages(packages: Set<String>) {
        dataStore.edit { it[BLOCKED_PACKAGES] = packages }
    }

    suspend fun saveBlockedDomains(domains: Set<String>) {
        dataStore.edit { it[BLOCKED_DOMAINS] = domains }
    }

    suspend fun saveBlockedKeywords(keywords: Set<String>) {
        dataStore.edit { it[BLOCKED_KEYWORDS] = keywords }
    }

    suspend fun saveSchedules(schedulesJson: String) {
        dataStore.edit { it[SCHEDULES] = schedulesJson }
    }

    suspend fun markConsentGiven() {
        dataStore.edit { it[CONSENT_GIVEN] = true }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
