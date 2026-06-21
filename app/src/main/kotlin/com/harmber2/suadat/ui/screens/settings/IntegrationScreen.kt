/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.harmber2.suadat.LocalPlayerAwareWindowInsets
import com.harmber2.suadat.R
import com.harmber2.suadat.constants.ListenBrainzEnabledKey
import com.harmber2.suadat.constants.ListenBrainzTokenKey
import com.harmber2.suadat.constants.ShowSpotifyPlaylistsKey
import com.harmber2.suadat.spotify.SpotifyAccountViewModel
import com.harmber2.suadat.ui.component.IconButton
import com.harmber2.suadat.ui.component.InfoLabel
import com.harmber2.suadat.ui.component.PreferenceEntry
import com.harmber2.suadat.ui.component.PreferenceGroup
import com.harmber2.suadat.ui.component.SwitchPreference
import com.harmber2.suadat.ui.component.TextFieldDialog
import com.harmber2.suadat.ui.utils.backToMain
import com.harmber2.suadat.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrationScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    spotifyAccountViewModel: SpotifyAccountViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val (listenBrainzEnabled, onListenBrainzEnabledChange) = rememberPreference(ListenBrainzEnabledKey, false)
    val (listenBrainzToken, onListenBrainzTokenChange) = rememberPreference(ListenBrainzTokenKey, "")

    var showListenBrainzTokenEditor = remember { mutableStateOf(false) }

    val spotifyState by spotifyAccountViewModel.uiState.collectAsStateWithLifecycle()
    val (showSpotifyPlaylists, onShowSpotifyPlaylistsChange) = rememberPreference(ShowSpotifyPlaylistsKey, true)
    var showSpotifyLogin by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(spotifyState.isAuthenticated) {
        if (spotifyState.isAuthenticated) {
            showSpotifyLogin = false
        }
    }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Top,
                ),
            ),
        )

        PreferenceGroup(title = stringResource(R.string.general)) {
            item {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.discord_integration)) },
                    icon = { Icon(painterResource(R.drawable.discord), null) },
                    onClick = {
                        navController.navigate("settings/discord")
                    },
                )
            }
        }

        PreferenceGroup(title = stringResource(R.string.spotify_account)) {
            spotifyAccountPreferences(
                state = spotifyState,
                showPlaylists = showSpotifyPlaylists,
                onConnectClick = { showSpotifyLogin = true },
                onShowPlaylistsChange = onShowSpotifyPlaylistsChange,
                onReloadClick = spotifyAccountViewModel::reloadPlaylists,
                onLogoutClick = {
                    spotifyAccountViewModel.logout()
                },
            )
        }

        PreferenceGroup(title = stringResource(R.string.scrobbling)) {
            item {
                PreferenceEntry(
                    title = { Text(stringResource(R.string.lastfm_integration)) },
                    icon = { Icon(painterResource(R.drawable.token), null) },
                    onClick = {
                        navController.navigate("settings/lastfm")
                    },
                )
            }

            item {
                SwitchPreference(
                    title = { Text(stringResource(R.string.listenbrainz_scrobbling)) },
                    description = stringResource(R.string.listenbrainz_scrobbling_description),
                    icon = { Icon(painterResource(R.drawable.token), null) },
                    checked = listenBrainzEnabled,
                    onCheckedChange = onListenBrainzEnabledChange,
                )
            }

            item {
                PreferenceEntry(
                    title = {
                        Text(
                            if (listenBrainzToken.isBlank()) {
                                stringResource(
                                    R.string.set_listenbrainz_token,
                                )
                            } else {
                                stringResource(R.string.edit_listenbrainz_token)
                            },
                        )
                    },
                    icon = { Icon(painterResource(R.drawable.token), null) },
                    onClick = { showListenBrainzTokenEditor.value = true },
                )
            }
        }
    }

    TopAppBar(
        title = { Text(stringResource(R.string.integration)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )

    if (showListenBrainzTokenEditor.value) {
        TextFieldDialog(
            initialTextFieldValue =
                androidx.compose.ui.text.input
                    .TextFieldValue(listenBrainzToken),
            onDone = { data ->
                onListenBrainzTokenChange(data)
                showListenBrainzTokenEditor.value = false
            },
            onDismiss = { showListenBrainzTokenEditor.value = false },
            singleLine = true,
            maxLines = 1,
            isInputValid = {
                it.isNotEmpty()
            },
            extraContent = {
                InfoLabel(text = stringResource(R.string.listenbrainz_scrobbling_description))
            },
        )
    }

    if (showSpotifyLogin) {
        SpotifyLoginSheet(
            onDismiss = { showSpotifyLogin = false },
            onCookiesCaptured = { spDc, spKey ->
                showSpotifyLogin = false
                spotifyAccountViewModel.connectWithCookies(spDc = spDc, spKey = spKey)
            },
        )
    }

    spotifyState.errorMessage?.let { error ->
        SpotifyErrorDialog(
            message = error,
            onDismiss = spotifyAccountViewModel::dismissError,
        )
    }
}
