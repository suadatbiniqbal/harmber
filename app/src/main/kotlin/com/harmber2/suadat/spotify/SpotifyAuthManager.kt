/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.spotify

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.harmber2.suadat.constants.SpotifyAccessTokenExpiresAtKey
import com.harmber2.suadat.constants.SpotifyAccessTokenKey
import com.harmber2.suadat.constants.SpotifySpDcKey
import com.harmber2.suadat.constants.SpotifySpKeyKey
import com.harmber2.suadat.spotify.Spotify
import com.harmber2.suadat.spotify.SpotifyAuth
import com.harmber2.suadat.utils.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object SpotifyAuthManager {
    private val mutex = Mutex()
    private const val EXPIRY_GRACE_MS = 60_000L

    suspend fun getAccessToken(context: Context): String? = mutex.withLock {
        val prefs = context.dataStore.data.first()
        val token = prefs[SpotifyAccessTokenKey]?.trim()
        val expiresAt = prefs[SpotifyAccessTokenExpiresAtKey] ?: 0L

        if (!token.isNullOrBlank() && expiresAt > System.currentTimeMillis() + EXPIRY_GRACE_MS) {
            Spotify.accessToken = token
            return token
        }

        val spDc = prefs[SpotifySpDcKey]?.trim().orEmpty()
        if (spDc.isBlank()) return null

        val spKey = prefs[SpotifySpKeyKey]?.trim().orEmpty()
        refreshAccessToken(context, spDc, spKey).getOrNull()
    }

    suspend fun refreshAccessToken(context: Context, spDc: String, spKey: String): Result<String> =
        withContext(Dispatchers.IO) {
            SpotifyAuth.fetchAccessToken(spDc, spKey).mapCatching { token ->
                context.dataStore.edit { prefs ->
                    prefs[SpotifyAccessTokenKey] = token.accessToken
                    prefs[SpotifyAccessTokenExpiresAtKey] = token.accessTokenExpirationTimestampMs
                }
                Spotify.accessToken = token.accessToken
                token.accessToken
            }
        }
    
    fun isAuthenticated(): Boolean = Spotify.isAuthenticated()
}
