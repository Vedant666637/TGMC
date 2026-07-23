package com.tgm.tgmc.feature.splash

import androidx.lifecycle.ViewModel
import com.tgm.tgmc.core.data.local.TgmcDataStore
import com.tgm.tgmc.core.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    dataStore: TgmcDataStore
) : ViewModel() {
    val userRole: Flow<UserRole> = dataStore.userRole
}
