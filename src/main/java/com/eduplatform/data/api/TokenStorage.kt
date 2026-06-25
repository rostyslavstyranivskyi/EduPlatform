package com.eduplatform.data.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        val USER_ROLE_KEY = stringPreferencesKey("user_role")
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }
    val userRole: Flow<String?> = context.dataStore.data.map { it[USER_ROLE_KEY] }
    val userId: Flow<String?> = context.dataStore.data.map { it[USER_ID_KEY] }
    val userName: Flow<String?> = context.dataStore.data.map { it[USER_NAME_KEY] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }

    // Synchronous snapshot for use inside the OkHttp interceptor (which runs on a
    // background thread per-request and must not block on a cold DataStore read).
    // We initialize it eagerly from disk and keep it updated via saveAuth()/clear().
    @Volatile
    var cachedAccessToken: String? = null
        private set

    init {
        // Best-effort initial sync load so a fresh process restart with an
        // already-saved session still attaches the token on the very first request.
        cachedAccessToken = try {
            runBlocking { accessToken.first() }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveAuth(token: String, role: String, id: String, name: String, refreshToken: String? = null) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN_KEY] = token
            prefs[USER_ROLE_KEY] = role
            prefs[USER_ID_KEY] = id
            prefs[USER_NAME_KEY] = name
            if (refreshToken != null) prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
        cachedAccessToken = token
    }

    suspend fun getRefreshToken(): String? =
        try { refreshToken.first() } catch (e: Exception) { null }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
        cachedAccessToken = null
    }
}