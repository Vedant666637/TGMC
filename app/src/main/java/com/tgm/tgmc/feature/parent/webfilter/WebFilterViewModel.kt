package com.tgm.tgmc.feature.parent.webfilter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tgm.tgmc.core.data.remote.TgmcApiService
import com.tgm.tgmc.core.domain.repository.DeviceRepository
import com.tgm.tgmc.core.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WebFilterRuleUi(
    val id: String,
    val ruleType: String,
    val value: String,
    val isBlocked: Boolean
)

data class WebFilterUiState(
    val deviceId: String? = null,
    val rules: List<WebFilterRuleUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WebFilterViewModel @Inject constructor(
    private val apiService: TgmcApiService,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WebFilterUiState())
    val uiState: StateFlow<WebFilterUiState> = _uiState.asStateFlow()

    init {
        loadDeviceThenRules()
    }

    private fun loadDeviceThenRules() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = deviceRepository.getDevices()) {
                is Result.Success -> {
                    val device = result.data.firstOrNull { it.isOnline } ?: result.data.firstOrNull()
                    if (device == null) {
                        _uiState.update { it.copy(isLoading = false, error = "Please pair a child device first.") }
                        return@launch
                    }
                    _uiState.update { it.copy(deviceId = device.deviceId) }
                    loadRules()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> { /* Handled via flow or ignore */ }
            }
        }
    }

    fun loadRules() {
        val deviceId = _uiState.value.deviceId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = apiService.getWebFilterRules(deviceId)
                if (response.isSuccessful) {
                    val rules = response.body()?.map {
                        WebFilterRuleUi(
                            id = it.id,
                            ruleType = it.ruleType,
                            value = it.value,
                            isBlocked = it.isBlocked
                        )
                    } ?: emptyList()
                    _uiState.update { it.copy(isLoading = false, rules = rules) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load filter rules") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun addRule(ruleType: String, value: String) {
        val deviceId = _uiState.value.deviceId ?: return
        viewModelScope.launch {
            try {
                apiService.addWebFilterRule(deviceId, mapOf(
                    "ruleType" to ruleType,
                    "value" to value,
                    "isBlocked" to true
                ))
                loadRules()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to add rule: ${e.message}") }
            }
        }
    }

    fun deleteRule(ruleId: String) {
        val deviceId = _uiState.value.deviceId ?: return
        viewModelScope.launch {
            // Optimistic removal
            _uiState.update { state ->
                state.copy(rules = state.rules.filter { it.id != ruleId })
            }
            try {
                apiService.deleteWebFilterRule(deviceId, ruleId)
            } catch (e: Exception) {
                loadRules() // Revert on error
            }
        }
    }

    fun seedDefaults() {
        val deviceId = _uiState.value.deviceId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                apiService.seedWebFilterDefaults(deviceId)
                loadRules()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to apply defaults: ${e.message}") }
            }
        }
    }
}
