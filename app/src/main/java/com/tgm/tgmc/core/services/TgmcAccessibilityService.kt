package com.tgm.tgmc.core.services

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.tgm.tgmc.core.data.local.TgmcDataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Calendar

data class TimeSchedule(
    val startHour: Int, val startMinute: Int,
    val endHour: Int, val endMinute: Int,
    val activeDays: List<Int>
)

/**
 * Accessibility Service for app usage monitoring AND web content filtering on the Child App.
 *
 * Responsibilities:
 * 1. App Blocking — intercepts TYPE_WINDOW_STATE_CHANGED to block banned apps
 * 2. Web Content Filtering — intercepts TYPE_WINDOW_CONTENT_CHANGED to read the browser
 *    URL bar and block restricted domains/keywords in real time
 */
@AndroidEntryPoint
class TgmcAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var dataStore: TgmcDataStore

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var blockedAppsCache = emptySet<String>()
    private var blockedDomainsCache = emptySet<String>()
    private var blockedKeywordsCache = emptySet<String>()
    private var schedulesCache = emptyList<TimeSchedule>()

    // Track the last blocked URL to avoid spamming the overlay
    private var lastBlockedUrl: String? = null
    // Track current package for periodic schedule checking
    private var currentPackageName: String? = null

    // Known browser packages and their URL bar view IDs
    private val browserUrlBarIds = mapOf(
        "com.android.chrome" to "com.android.chrome:id/url_bar",
        "com.chrome.canary" to "com.chrome.canary:id/url_bar",
        "com.chrome.beta" to "com.chrome.beta:id/url_bar",
        "com.chrome.dev" to "com.chrome.dev:id/url_bar",
        "org.mozilla.firefox" to "org.mozilla.firefox:id/url_bar_title",
        "org.mozilla.firefox_beta" to "org.mozilla.firefox_beta:id/url_bar_title",
        "com.opera.browser" to "com.opera.browser:id/url_field",
        "com.opera.mini.native" to "com.opera.mini.native:id/url_field",
        "com.brave.browser" to "com.brave.browser:id/url_bar",
        "com.microsoft.emmx" to "com.microsoft.emmx:id/url_bar",
        "com.samsung.android.app.sbrowser" to "com.sec.android.app.sbrowser:id/location_bar_edit_text",
        "com.vivaldi.browser" to "com.vivaldi.browser:id/url_bar",
        "com.duckduckgo.mobile.android" to "com.duckduckgo.mobile.android:id/omnibarTextInput"
    )

    // Known browser package names (broader set for fallback detection)
    private val knownBrowserPackages = setOf(
        "com.android.chrome", "com.chrome.canary", "com.chrome.beta", "com.chrome.dev",
        "org.mozilla.firefox", "org.mozilla.firefox_beta", "org.mozilla.focus",
        "com.opera.browser", "com.opera.mini.native",
        "com.brave.browser", "com.microsoft.emmx",
        "com.samsung.android.app.sbrowser", "com.sec.android.app.sbrowser",
        "com.vivaldi.browser", "com.duckduckgo.mobile.android",
        "com.kiwibrowser.browser", "com.UCMobile.intl"
    )

    companion object {
        private const val TAG = "TgmcAccessibility"
    }

    override fun onCreate() {
        super.onCreate()
        // Collect blocked apps
        serviceScope.launch {
            dataStore.blockedPackages.collectLatest { packages ->
                blockedAppsCache = packages
            }
        }
        // Collect blocked domains
        serviceScope.launch {
            dataStore.blockedDomains.collectLatest { domains ->
                blockedDomainsCache = domains
                Log.d(TAG, "Blocked domains updated: ${domains.size} rules")
            }
        }
        // Collect blocked keywords
        serviceScope.launch {
            dataStore.blockedKeywords.collectLatest { keywords ->
                blockedKeywordsCache = keywords
                Log.d(TAG, "Blocked keywords updated: ${keywords.size} rules")
            }
        }
        // Collect schedules
        serviceScope.launch {
            dataStore.schedules.collectLatest { schedulesJson ->
                try {
                    val jsonArray = org.json.JSONArray(schedulesJson)
                    val list = mutableListOf<TimeSchedule>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val daysArray = try { org.json.JSONArray(obj.getString("activeDays")) } catch (e: Exception) { null }
                        val activeDays = mutableListOf<Int>()
                        if (daysArray != null) {
                            for (j in 0 until daysArray.length()) {
                                activeDays.add(daysArray.getInt(j))
                            }
                        }
                        list.add(TimeSchedule(
                            startHour = obj.getInt("startHour"),
                            startMinute = obj.getInt("startMinute"),
                            endHour = obj.getInt("endHour"),
                            endMinute = obj.getInt("endMinute"),
                            activeDays = activeDays
                        ))
                    }
                    schedulesCache = list
                    Log.d(TAG, "Schedules updated: ${list.size} rules")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse schedules", e)
                }
            }
        }
        
        // Background checker for active schedules
        serviceScope.launch {
            while (true) {
                if (isTimeScheduleActive() && isAppBlockable(currentPackageName)) {
                    launchAppBlockOverlay("It is currently downtime. Please put your device away.")
                }
                kotlinx.coroutines.delay(30_000)
            }
        }
    }

    private fun isAppBlockable(packageName: String?): Boolean {
        if (packageName == null) return false
        if (packageName == this.packageName ||
            packageName == "com.android.settings" ||
            packageName == "com.google.android.packageinstaller" ||
            packageName.contains("launcher")
        ) {
            return false
        }
        return true
    }

    override fun onServiceConnected() {
        serviceInfo = AccessibilityServiceInfo().apply {
            // Listen to both window state changes (app switching) AND content changes (URL bar updates)
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val evt = event ?: return
        val packageName = evt.packageName?.toString() ?: return

        if (!isAppBlockable(packageName)) {
            return
        }

        currentPackageName = packageName

        when (evt.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                // ── App & Schedule Blocking ──
                if (isTimeScheduleActive()) {
                    launchAppBlockOverlay("It is currently downtime. Please put your device away.")
                    return
                }
                if (blockedAppsCache.contains(packageName)) {
                    launchAppBlockOverlay("This app has been blocked by your parent.")
                    return
                }
                // Reset last blocked URL when switching apps
                lastBlockedUrl = null
            }

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                // ── Web Content Filtering ──
                if (knownBrowserPackages.contains(packageName)) {
                    handleBrowserContentChange(evt, packageName)
                }
            }
        }
    }

    /**
     * Extracts the current URL from a browser's address bar and checks it
     * against the blocked domains and keywords lists.
     */
    private fun handleBrowserContentChange(event: AccessibilityEvent, browserPackage: String) {
        val rootNode = rootInActiveWindow ?: return

        // Try to find the URL bar by known view ID first (fastest path)
        val urlBarId = browserUrlBarIds[browserPackage]
        var currentUrl: String? = null

        if (urlBarId != null) {
            val urlNodes = rootNode.findAccessibilityNodeInfosByViewId(urlBarId)
            if (urlNodes.isNotEmpty()) {
                currentUrl = urlNodes[0].text?.toString()
            }
        }

        // Fallback: traverse the node tree looking for an EditText with URL-like content
        if (currentUrl == null) {
            currentUrl = findUrlInNodeTree(rootNode)
        }

        rootNode.recycle()

        if (currentUrl.isNullOrBlank()) return

        // Normalize the URL
        val normalizedUrl = normalizeUrl(currentUrl)

        // Skip if we already blocked this exact URL (prevents overlay spam)
        if (normalizedUrl == lastBlockedUrl) return

        // ── Check domain match ──
        val domain = extractDomain(normalizedUrl)
        if (domain != null && isDomainBlocked(domain)) {
            Log.i(TAG, "BLOCKED domain: $domain (URL: $normalizedUrl)")
            lastBlockedUrl = normalizedUrl
            launchWebBlockOverlay("This website ($domain) has been blocked by your parent.")
            return
        }

        // ── Check keyword match ──
        val matchedKeyword = findBlockedKeyword(normalizedUrl)
        if (matchedKeyword != null) {
            Log.i(TAG, "BLOCKED keyword '$matchedKeyword' found in URL: $normalizedUrl")
            lastBlockedUrl = normalizedUrl
            launchWebBlockOverlay("This page contains blocked content (\"$matchedKeyword\").")
            return
        }
    }

    /**
     * Traverses the accessibility node tree looking for an EditText
     * that contains URL-like text (has a dot and looks like a web address).
     */
    private fun findUrlInNodeTree(node: AccessibilityNodeInfo): String? {
        if (node.className?.toString() == "android.widget.EditText") {
            val text = node.text?.toString()
            if (text != null && text.contains(".") &&
                (text.contains("http") || text.contains("www") || !text.contains(" "))
            ) {
                return text
            }
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findUrlInNodeTree(child)
            child.recycle()
            if (result != null) return result
        }
        return null
    }

    /**
     * Normalizes a URL string — strips protocol, www prefix, and trailing slashes.
     */
    private fun normalizeUrl(url: String): String {
        return url.lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .trimEnd('/')
    }

    /**
     * Extracts the domain from a normalized URL.
     * e.g. "example.com/path/page" → "example.com"
     */
    private fun extractDomain(normalizedUrl: String): String? {
        val slashIndex = normalizedUrl.indexOf('/')
        val domain = if (slashIndex > 0) normalizedUrl.substring(0, slashIndex) else normalizedUrl
        // Basic sanity check: must have at least one dot
        return if (domain.contains(".")) domain else null
    }

    /**
     * Checks if a domain matches any blocked domain rule.
     * Supports subdomain matching: blocking "facebook.com" also blocks "m.facebook.com".
     */
    private fun isDomainBlocked(domain: String): Boolean {
        return blockedDomainsCache.any { blockedDomain ->
            domain == blockedDomain || domain.endsWith(".$blockedDomain")
        }
    }

    /**
     * Scans the URL for any blocked keyword.
     * Returns the first matched keyword, or null if none found.
     */
    private fun findBlockedKeyword(normalizedUrl: String): String? {
        return blockedKeywordsCache.firstOrNull { keyword ->
            normalizedUrl.contains(keyword, ignoreCase = true)
        }
    }

    /**
     * Checks if the current time falls within any active time schedule.
     */
    private fun isTimeScheduleActive(): Boolean {
        if (schedulesCache.isEmpty()) return false
        
        val cal = Calendar.getInstance()
        // JS uses 0=Sunday, 1=Monday... Calendar uses 1=Sunday, 2=Monday...
        val currentDay = cal.get(Calendar.DAY_OF_WEEK) - 1 
        val currentTotalMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        
        for (schedule in schedulesCache) {
            if (schedule.activeDays.contains(currentDay)) {
                val startTotal = schedule.startHour * 60 + schedule.startMinute
                val endTotal = schedule.endHour * 60 + schedule.endMinute
                
                if (startTotal <= endTotal) {
                    if (currentTotalMinutes in startTotal until endTotal) return true
                } else {
                    // Handles overnight schedules (e.g. 22:00 to 06:00)
                    if (currentTotalMinutes >= startTotal || currentTotalMinutes < endTotal) return true
                }
            }
        }
        return false
    }

    private fun launchAppBlockOverlay(reason: String) {
        val intent = Intent(this, AppBlockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("block_reason", reason)
        }
        startActivity(intent)
    }

    private fun launchWebBlockOverlay(reason: String) {
        val intent = Intent(this, WebBlockActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("block_reason", reason)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        // Required override
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
