package com.lisitede.preset.preset

import kotlinx.coroutines.delay
import java.util.UUID

class AuthRepository {

    suspend fun login(username: String, password: String): Result<Pair<String, String>> {
        delay(500)
        return if (username.isNotBlank() && password.isNotBlank()) {
            val token = UUID.randomUUID().toString()
            Result.success(token to username)
        } else {
            Result.failure(IllegalArgumentException("Username and password must not be empty"))
        }
    }
}
