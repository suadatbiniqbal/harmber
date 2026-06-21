/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.harmber2.suadat.playback.MusicService

class PlayPauseAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        sendWidgetAction(context, ACTION_PLAY_PAUSE)
    }
}

class SkipNextAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        sendWidgetAction(context, ACTION_SKIP_NEXT)
    }
}

class SkipPrevAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        sendWidgetAction(context, ACTION_SKIP_PREV)
    }
}

private const val ACTION_PLAY_PAUSE = "com.harmber2.suadat.WIDGET_PLAY_PAUSE"
private const val ACTION_SKIP_NEXT = "com.harmber2.suadat.WIDGET_SKIP_NEXT"
private const val ACTION_SKIP_PREV = "com.harmber2.suadat.WIDGET_SKIP_PREV"
private const val TAG = "MusicWidgetActions"

private fun sendWidgetAction(
    context: Context,
    action: String,
) {
    val intent = Intent(action).setClass(context, MusicService::class.java)
    runCatching {
        context.startService(intent)
    }.onFailure { error ->
        Log.e(TAG, "Failed to send widget action: $action", error)
    }
}
