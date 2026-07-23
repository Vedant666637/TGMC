package com.tgm.tgmc.feature.parent.appblock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tgm.tgmc.core.domain.model.AppCategory
import com.tgm.tgmc.core.domain.model.AppInfo
import com.tgm.tgmc.core.domain.repository.AppBlockRepository
import com.tgm.tgmc.core.domain.repository.DeviceRepository
import com.tgm.tgmc.core.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppBlockUiState(
    val deviceId: String? = null,
    val apps: List<AppInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AppBlockViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val appBlockRepository: AppBlockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppBlockUiState())
    val uiState: StateFlow<AppBlockUiState> = _uiState.asStateFlow()

    init {
        loadDeviceAndApps()
    }

    fun loadDeviceAndApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // 1. Get active child device
            when (val deviceResult = deviceRepository.getDevices()) {
                is Result.Success -> {
                    val activeDevice = deviceResult.data.firstOrNull { it.isOnline } ?: deviceResult.data.firstOrNull()
                    if (activeDevice == null) {
                        _uiState.update { it.copy(isLoading = false, error = "Please pair a child device first.") }
                        return@launch
                    }
                    _uiState.update { it.copy(deviceId = activeDevice.deviceId) }
                    
                    // 2. Load apps for this device
                    loadApps(activeDevice.deviceId)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = deviceResult.message) }
                }
                is Result.Loading -> { /* Handled via flow or ignore */ }
            }
        }
    }

    private suspend fun loadApps(deviceId: String) {
        when (val appsResult = appBlockRepository.getBlockRules(deviceId)) {
            is Result.Success -> {
                _uiState.update { it.copy(isLoading = false, apps = appsResult.data) }
            }
            is Result.Error -> {
                _uiState.update { it.copy(isLoading = false, error = appsResult.message) }
            }
                is Result.Loading -> { /* Handled via flow or ignore */ }
        }
    }

    fun toggleAppBlock(app: AppInfo) {
        val deviceId = _uiState.value.deviceId ?: return
        val updatedBlockStatus = !app.isBlocked
        
        viewModelScope.launch {
            // Update local memory list instantly for smooth responsive UI
            _uiState.update { state ->
                state.copy(
                    apps = state.apps.map { 
                        if (it.packageName == app.packageName) it.copy(isBlocked = updatedBlockStatus) else it 
                    }
                )
            }

            // Sync with backend API
            val result = if (updatedBlockStatus) {
                appBlockRepository.setBlockRule(deviceId, app.copy(isBlocked = true))
            } else {
                appBlockRepository.removeBlockRule(deviceId, app.packageName)
            }

            if (result is Result.Error) {
                // Revert on error
                _uiState.update { state ->
                    state.copy(
                        apps = state.apps.map { 
                            if (it.packageName == app.packageName) it.copy(isBlocked = app.isBlocked) else it 
                        },
                        error = "Failed to sync rule: ${result.message}"
                    )
                }
            }
        }
    }
}
