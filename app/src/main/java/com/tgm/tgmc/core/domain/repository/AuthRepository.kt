package com.tgm.tgmc.core.domain.repository

import com.tgm.tgmc.core.data.remote.LoginRequest
import com.tgm.tgmc.core.data.remote.RegisterRequest
import com.tgm.tgmc.core.domain.model.AuthToken
import com.tgm.tgmc.core.domain.model.UserRole
import com.tgm.tgmc.core.util.Result

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthToken>
    suspend fun googleLogin(idToken: String): Result<AuthToken>
    suspend fun register(email: String, password: String, displayName: String?): Result<AuthToken>
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun logout()
    suspend fun getCurrentRole(): UserRole
}
