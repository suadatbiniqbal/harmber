/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.utils

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import com.harmber2.suadat.constants.AccountChannelHandleKey
import com.harmber2.suadat.constants.AccountEmailKey
import com.harmber2.suadat.constants.AccountNameKey
import com.harmber2.suadat.constants.DataSyncIdKey
import com.harmber2.suadat.constants.InnerTubeCookieKey
import com.harmber2.suadat.constants.PoTokenGvsKey
import com.harmber2.suadat.constants.PoTokenKey
import com.harmber2.suadat.constants.PoTokenPlayerKey
import com.harmber2.suadat.constants.PoTokenSourceUrlKey
import com.harmber2.suadat.constants.VisitorDataKey
import com.harmber2.suadat.constants.WebClientPoTokenEnabledKey
import com.harmber2.suadat.innertube.PlaybackAuthState
import com.harmber2.suadat.innertube.YouTube

fun Preferences.toPlaybackAuthState(): PlaybackAuthState =
    PlaybackAuthState(
        cookie = this[InnerTubeCookieKey],
        visitorData = this[VisitorDataKey],
        dataSyncId = this[DataSyncIdKey],
        poToken = this[PoTokenKey],
        poTokenGvs = this[PoTokenGvsKey],
        poTokenPlayer = this[PoTokenPlayerKey],
        webClientPoTokenEnabled = this[WebClientPoTokenEnabledKey] ?: false,
    ).normalized()

fun MutablePreferences.clearPlaybackAuthSession(clearAccountIdentity: Boolean = true) {
    remove(InnerTubeCookieKey)
    remove(VisitorDataKey)
    remove(DataSyncIdKey)
    remove(PoTokenKey)
    remove(PoTokenGvsKey)
    remove(PoTokenPlayerKey)
    remove(PoTokenSourceUrlKey)
    if (clearAccountIdentity) {
        remove(AccountNameKey)
        remove(AccountEmailKey)
        remove(AccountChannelHandleKey)
    }
}

fun MutablePreferences.clearPlaybackLoginContext() {
    remove(DataSyncIdKey)
}

fun PlaybackAuthState.withoutPlaybackLoginContext(): PlaybackAuthState = copy(dataSyncId = null).normalized()

fun MutablePreferences.putLegacyPoToken(value: String?) {
    val normalized = value?.trim()?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
    if (normalized == null) {
        remove(PoTokenKey)
    } else {
        this[PoTokenKey] = normalized
    }
    remove(PoTokenGvsKey)
    remove(PoTokenPlayerKey)
}

suspend fun Context.resetPlaybackLoginContext(): PlaybackAuthState {
    dataStore.edit { preferences ->
        preferences.clearPlaybackLoginContext()
    }
    val authState = dataStore.data.first().toPlaybackAuthState()
    YouTube.authState = authState
    YTPlayerUtils.clearPlaybackAuthCaches()
    return authState
}

suspend fun <T> Context.retryWithoutPlaybackLoginContext(block: suspend () -> Result<T>): Result<T> {
    val initialAuthState = YouTube.currentPlaybackAuthState()
    val initialResult = block()
    if (initialResult.isSuccess) {
        persistPlaybackAuthRepair(
            initialAuthState = initialAuthState,
            repairedAuthState = YouTube.currentPlaybackAuthState(),
        )
        return initialResult
    }
    val failure = initialResult.exceptionOrNull()

    val currentAuthState = YouTube.currentPlaybackAuthState()
    if (!shouldRetryWithoutPlaybackLoginContext(initialAuthState, currentAuthState, failure)) {
        return initialResult
    }

    YouTube.authState = currentAuthState.withoutPlaybackLoginContext()
    YTPlayerUtils.clearPlaybackAuthCaches()
    dataStore.edit { preferences ->
        preferences.remove(DataSyncIdKey)
    }
    val retryResult = block()
    if (retryResult.isSuccess) {
        persistPlaybackAuthRepair(
            initialAuthState = currentAuthState,
            repairedAuthState = YouTube.currentPlaybackAuthState(),
        )
    }
    return retryResult
}

private suspend fun Context.persistPlaybackAuthRepair(
    initialAuthState: PlaybackAuthState,
    repairedAuthState: PlaybackAuthState,
) {
    if (initialAuthState.cookie != repairedAuthState.cookie) return
    if (initialAuthState.fingerprint == repairedAuthState.fingerprint) return

    dataStore.edit { preferences ->
        repairedAuthState.visitorData
            ?.takeIf { it.isNotBlank() && it != initialAuthState.visitorData }
            ?.let { preferences[VisitorDataKey] = it }
        repairedAuthState.dataSyncId
            ?.takeIf { it.isNotBlank() && it != initialAuthState.dataSyncId }
            ?.let { preferences[DataSyncIdKey] = it }
            ?: run {
                if (!initialAuthState.dataSyncId.isNullOrBlank() && repairedAuthState.dataSyncId.isNullOrBlank()) {
                    preferences.remove(DataSyncIdKey)
                }
            }
    }
}

internal fun shouldRetryWithoutPlaybackLoginContext(
    initialAuthState: PlaybackAuthState,
    currentAuthState: PlaybackAuthState,
    failure: Throwable?,
): Boolean {
    if (failure !is YTPlayerUtils.InvalidPlaybackLoginContextException) return false
    if (!initialAuthState.hasPlaybackLoginContext) return false
    if (!currentAuthState.hasPlaybackLoginContext) return false
    if (currentAuthState.cookie != initialAuthState.cookie) return false
    if (currentAuthState.dataSyncId != initialAuthState.dataSyncId) return false
    return true
}
