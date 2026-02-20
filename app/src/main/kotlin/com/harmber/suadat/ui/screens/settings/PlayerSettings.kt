package com.harmber.suadat.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.harmber.suadat.LocalPlayerAwareWindowInsets
import com.harmber.suadat.R
import com.harmber.suadat.constants.ArtistSeparatorsKey
import com.harmber.suadat.constants.AudioNormalizationKey
import com.harmber.suadat.constants.AudioQuality
import com.harmber.suadat.constants.AudioQualityKey
import com.harmber.suadat.constants.NetworkMeteredKey
import com.harmber.suadat.constants.AutoDownloadOnLikeKey
import com.harmber.suadat.constants.AutoLoadMoreKey
import com.harmber.suadat.constants.AutoSkipNextOnErrorKey
import com.harmber.suadat.constants.PermanentShuffleKey
import com.harmber.suadat.constants.PersistentQueueKey
import com.harmber.suadat.constants.SimilarContent
import com.harmber.suadat.constants.SkipSilenceKey
import com.harmber.suadat.constants.StopMusicOnTaskClearKey
import com.harmber.suadat.constants.HistoryDuration
import com.harmber.suadat.constants.PlayerStreamClient
import com.harmber.suadat.constants.PlayerStreamClientKey
import com.harmber.suadat.constants.SeekExtraSeconds
import com.harmber.suadat.ui.component.ArtistSeparatorsDialog
import com.harmber.suadat.ui.component.TagsManagementDialog
import com.harmber.suadat.ui.component.EnumListPreference
import com.harmber.suadat.ui.component.IconButton
import com.harmber.suadat.ui.component.ListDialog
import com.harmber.suadat.ui.component.PreferenceEntry
import com.harmber.suadat.ui.component.PreferenceGroupTitle
import com.harmber.suadat.ui.component.SliderPreference
import com.harmber.suadat.ui.component.SwitchPreference
import com.harmber.suadat.ui.utils.backToMain
import com.harmber.suadat.utils.rememberEnumPreference
import com.harmber.suadat.utils.rememberPreference
import com.harmber.suadat.LocalDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (audioQuality, onAudioQualityChange) = rememberEnumPreference(
        AudioQualityKey,
        defaultValue = AudioQuality.AUTO
    )
    val (playerStreamClient, onPlayerStreamClientChange) = rememberEnumPreference(
        PlayerStreamClientKey,
        defaultValue = PlayerStreamClient.ANDROID_VR
    )
    val (networkMetered, onNetworkMeteredChange) = rememberPreference(
        NetworkMeteredKey,
        defaultValue = true
    )
    val (persistentQueue, onPersistentQueueChange) = rememberPreference(
        PersistentQueueKey,
        defaultValue = true
    )
    val (permanentShuffle, onPermanentShuffleChange) = rememberPreference(
        PermanentShuffleKey,
        defaultValue = false
    )
    val (skipSilence, onSkipSilenceChange) = rememberPreference(
        SkipSilenceKey,
        defaultValue = false
    )
    val (audioNormalization, onAudioNormalizationChange) = rememberPreference(
        AudioNormalizationKey,
        defaultValue = true
    )

    val (seekExtraSeconds, onSeekExtraSeconds) = rememberPreference(
        SeekExtraSeconds,
        defaultValue = false
    )

    val (autoLoadMore, onAutoLoadMoreChange) = rememberPreference(
        AutoLoadMoreKey,
        defaultValue = true
    )
    val (autoDownloadOnLike, onAutoDownloadOnLikeChange) = rememberPreference(
        AutoDownloadOnLikeKey,
        defaultValue = false
    )
    val (similarContentEnabled, similarContentEnabledChange) = rememberPreference(
        key = SimilarContent,
        defaultValue = true
    )
    val (autoSkipNextOnError, onAutoSkipNextOnErrorChange) = rememberPreference(
        AutoSkipNextOnErrorKey,
        defaultValue = false
    )
    val (stopMusicOnTaskClear, onStopMusicOnTaskClearChange) = rememberPreference(
        StopMusicOnTaskClearKey,
        defaultValue = false
    )
    val (historyDuration, onHistoryDurationChange) = rememberPreference(
        HistoryDuration,
        defaultValue = 30f
    )

    val (artistSeparators, onArtistSeparatorsChange) = rememberPreference(
        ArtistSeparatorsKey,
        defaultValue = ",;/&"
    )

    var showArtistSeparatorsDialog by remember { mutableStateOf(false) }
    var showTagsManagementDialog by remember { mutableStateOf(false) }
    var showPlayerStreamClientDialog by remember { mutableStateOf(false) }
    val database = LocalDatabase.current

    if (showArtistSeparatorsDialog) {
        ArtistSeparatorsDialog(
            currentSeparators = artistSeparators,
            onDismiss = { showArtistSeparatorsDialog = false },
            onSave = { newSeparators ->
                onArtistSeparatorsChange(newSeparators)
                showArtistSeparatorsDialog = false
            }
        )
    }

    if (showTagsManagementDialog) {
        TagsManagementDialog(
            database = database,
            onDismiss = { showTagsManagementDialog = false }
        )
    }

    if (showPlayerStreamClientDialog) {
        ListDialog(
            onDismiss = { showPlayerStreamClientDialog = false },
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            items(listOf(PlayerStreamClient.ANDROID_VR, PlayerStreamClient.WEB_REMIX)) { value ->
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            onPlayerStreamClientChange(value)
                            showPlayerStreamClientDialog = false
                        }.padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    RadioButton(
                        selected = value == playerStreamClient,
                        onClick = null,
                    )

                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(
                            text =
                            when (value) {
                                PlayerStreamClient.ANDROID_VR -> stringResource(R.string.player_stream_client_android_vr)
                                PlayerStreamClient.WEB_REMIX -> stringResource(R.string.player_stream_client_web_remix)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text =
                            when (value) {
                                PlayerStreamClient.ANDROID_VR -> stringResource(R.string.player_stream_client_android_vr_desc)
                                PlayerStreamClient.WEB_REMIX -> stringResource(R.string.player_stream_client_web_remix_desc)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    }

    Column(
        Modifier
            .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Top
                )
            )
        )

        PreferenceGroupTitle(
            title = stringResource(R.string.player)
        )

        EnumListPreference(
            title = { Text(stringResource(R.string.audio_quality)) },
            icon = { Icon(painterResource(R.drawable.graphic_eq), null) },
            selectedValue = audioQuality,
            onValueSelected = onAudioQualityChange,
            valueText = {
                when (it) {
                    AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                    AudioQuality.VERY_HIGH -> stringResource(R.string.audio_quality_very_high)
                    AudioQuality.HIGHEST -> stringResource(R.string.audio_quality_highest)
                    AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                    AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                }
            }
        )

        PreferenceEntry(
            title = { Text(stringResource(R.string.player_stream_client)) },
            description =
            when (playerStreamClient) {
                PlayerStreamClient.ANDROID_VR -> stringResource(R.string.player_stream_client_android_vr)
                PlayerStreamClient.WEB_REMIX -> stringResource(R.string.player_stream_client_web_remix)
            },
            icon = { Icon(painterResource(R.drawable.integration), null) },
            onClick = { showPlayerStreamClientDialog = true }
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.network_metered_title)) },
            description = stringResource(R.string.network_metered_description),
            icon = { Icon(painterResource(R.drawable.android_cell), null) },
            checked = networkMetered,
            onCheckedChange = onNetworkMeteredChange
        )

        SliderPreference(
            title = { Text(stringResource(R.string.history_duration)) },
            icon = { Icon(painterResource(R.drawable.history), null) },
            value = historyDuration,
            onValueChange = onHistoryDurationChange,
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.skip_silence)) },
            icon = { Icon(painterResource(R.drawable.fast_forward), null) },
            checked = skipSilence,
            onCheckedChange = onSkipSilenceChange
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.audio_normalization)) },
            icon = { Icon(painterResource(R.drawable.volume_up), null) },
            checked = audioNormalization,
            onCheckedChange = onAudioNormalizationChange
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.seek_seconds_addup)) },
            description = stringResource(R.string.seek_seconds_addup_description),
            icon = { Icon(painterResource(R.drawable.arrow_forward), null) },
            checked = seekExtraSeconds,
            onCheckedChange = onSeekExtraSeconds
        )

        PreferenceGroupTitle(
            title = stringResource(R.string.queue)
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.persistent_queue)) },
            description = stringResource(R.string.persistent_queue_desc),
            icon = { Icon(painterResource(R.drawable.queue_music), null) },
            checked = persistentQueue,
            onCheckedChange = onPersistentQueueChange
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.permanent_shuffle)) },
            description = stringResource(R.string.permanent_shuffle_desc),
            icon = { Icon(painterResource(R.drawable.shuffle), null) },
            checked = permanentShuffle,
            onCheckedChange = onPermanentShuffleChange
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.auto_load_more)) },
            description = stringResource(R.string.auto_load_more_desc),
            icon = { Icon(painterResource(R.drawable.playlist_add), null) },
            checked = autoLoadMore,
            onCheckedChange = onAutoLoadMoreChange
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.auto_download_on_like)) },
            description = stringResource(R.string.auto_download_on_like_desc),
            icon = { Icon(painterResource(R.drawable.download), null) },
            checked = autoDownloadOnLike,
            onCheckedChange = onAutoDownloadOnLikeChange
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.enable_similar_content)) },
            description = stringResource(R.string.similar_content_desc),
            icon = { Icon(painterResource(R.drawable.similar), null) },
            checked = similarContentEnabled,
            onCheckedChange = similarContentEnabledChange,
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.auto_skip_next_on_error)) },
            description = stringResource(R.string.auto_skip_next_on_error_desc),
            icon = { Icon(painterResource(R.drawable.skip_next), null) },
            checked = autoSkipNextOnError,
            onCheckedChange = onAutoSkipNextOnErrorChange
        )

        PreferenceGroupTitle(
            title = stringResource(R.string.misc)
        )

        SwitchPreference(
            title = { Text(stringResource(R.string.stop_music_on_task_clear)) },
            icon = { Icon(painterResource(R.drawable.clear_all), null) },
            checked = stopMusicOnTaskClear,
            onCheckedChange = onStopMusicOnTaskClearChange
        )

        PreferenceEntry(
            title = { Text(stringResource(R.string.artist_separators)) },
            description = artistSeparators.map { "\"$it\"" }.joinToString("  "),
            icon = { Icon(painterResource(R.drawable.artist), null) },
            onClick = { showArtistSeparatorsDialog = true }
        )

        PreferenceEntry(
            title = { Text(stringResource(R.string.manage_playlist_tags)) },
            description = stringResource(R.string.manage_playlist_tags_desc),
            icon = { Icon(painterResource(R.drawable.style), null) },
            onClick = { showTagsManagementDialog = true }
        )
    }

    TopAppBar(
        title = { Text(stringResource(R.string.player_and_audio)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain
            ) {
                Icon(
                    painterResource(R.drawable.arrow_back),
                    contentDescription = null
                )
            }
        }
    )
}
