package com.tgm.tgmc.core.data.repository

import com.tgm.tgmc.core.data.local.TgmcDataStore
import com.tgm.tgmc.core.data.remote.LoginRequest
import com.tgm.tgmc.core.data.remote.GoogleLoginRequest
import com.tgm.tgmc.core.data.remote.RegisterRequest
import com.tgm.tgmc.core.data.remote.ForgotPasswordRequest
import com.tgm.tgmc.core.data.remote.TgmcApiService
import com.tgm.tgmc.core.domain.model.AuthToken
import com.tgm.tgmc.core.domain.model.UserRole
import com.tgm.tgmc.core.domain.repository.AuthRepository
import com.tgm.tgmc.core.util.Result
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: TgmcApiService,
    private val dataStore: TgmcDataStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthToken> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!
                dataStore.saveAuthTokens(
                    accessToken  = token.accessToken,
                    refreshToken = token.refreshToken,
                    role         = token.role,
                    userId       = token.userId,
                    email        = token.email
                )
                Result.Success(token)
            } else {
                val code = response.code()
                val msg = when (code) {
                    401  -> "Invalid email or password"
                    403  -> "Account suspended. Contact support."
                    else -> "Login failed (code $code)"
                }
                Result.Error(msg, code)
            }
        } catch (e: Exception) {
            Result.Error("Connection Error: ${e.message}")
        }
    }

    override suspend fun googleLogin(idToken: String): Result<AuthToken> {
        return try {
            val response = api.googleLogin(GoogleLoginRequest(idToken))
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!
                dataStore.saveAuthTokens(
                    accessToken  = token.accessToken,
                    refreshToken = token.refreshToken,
                    role         = token.role,
                    userId       = token.userId,
                    email        = token.email
                )
                Result.Success(token)
            } else {
                Result.Error("Google Login failed (code ${response.code()})", response.code())
            }
        } catch (e: Exception) {
            Result.Error("Connection Error: ${e.message}")
        }
    }

    override suspend fun register(email: String, password: String, displayName: String?): Result<AuthToken> {
        return try {
            val response = api.register(RegisterRequest(email, password, displayName ?: ""))
            if (response.isSuccessful && response.body() != null) {
                val token = response.body()!!
                dataStore.saveAuthTokens(
                    accessToken  = token.accessToken,
                    refreshToken = token.refreshToken,
                    role         = token.role,
                    userId       = token.userId,
                    email        = token.email
                )
                Result.Success(token)
            } else {
                val code = response.code()
                val msg = when (code) {
                    409  -> "Email already registered. Try logging in."
                    else -> "Registration failed (code $code)"
                }
                Result.Error(msg, code)
            }
        } catch (e: Exception) {
            Result.Error("Connection Error: ${e.message}")
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        return try {
            api.forgotPassword(ForgotPasswordRequest(email))
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Request failed. Please try again.")
        }
    }

    override suspend fun logout() {
        dataStore.clearAll()
    }

    override suspend fun getCurrentRole(): UserRole {
        return dataStore.userRole.firstOrNull() ?: UserRole.NONE
    }
}
