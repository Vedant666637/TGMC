package com.tgm.tgmc.feature.child.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tgm.tgmc.core.data.remote.EducationalContent
import com.tgm.tgmc.core.data.remote.StoreItem
import com.tgm.tgmc.core.data.remote.TgmcApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import com.tgm.tgmc.core.data.local.TgmcDataStore

data class ChildContentUiState(
    val statuses: List<EducationalContent> = emptyList(),
    val feed: List<EducationalContent> = emptyList(),
    val storeItems: List<StoreItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChildContentViewModel @Inject constructor(
    private val apiService: TgmcApiService,
    private val dataStore: TgmcDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChildContentUiState())
    val uiState: StateFlow<ChildContentUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val statusesRes = apiService.getStatuses()
                val feedRes = apiService.getFeed()
                val storeRes = apiService.getStoreItems()

                _uiState.update {
                    it.copy(
                        statuses = if (statusesRes.isSuccessful) statusesRes.body() ?: emptyList() else emptyList(),
                        feed = if (feedRes.isSuccessful) feedRes.body() ?: emptyList() else emptyList(),
                        storeItems = if (storeRes.isSuccessful) storeRes.body() ?: emptyList() else emptyList(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unknown error")
                }
            }
        }
    }

    fun requestPurchase(itemId: String) {
        viewModelScope.launch {
            try {
                val deviceId = dataStore.deviceId.firstOrNull()
                if (deviceId != null) {
                    apiService.requestPurchase(deviceId, itemId)
                    // In a full implementation, you'd show a success toast to the user
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
