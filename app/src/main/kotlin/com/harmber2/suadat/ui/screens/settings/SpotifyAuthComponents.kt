/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.ui.screens.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.harmber2.suadat.R
import com.harmber2.suadat.constants.SpotifyCanvasEnabledKey
import com.harmber2.suadat.constants.SpotifyRecommendationsEnabledKey
import com.harmber2.suadat.spotify.SpotifyAccountUiState
import com.harmber2.suadat.spotify.SpotifyAuth
import com.harmber2.suadat.ui.component.DefaultDialog
import com.harmber2.suadat.ui.component.PreferenceEntry
import com.harmber2.suadat.ui.component.PreferenceGroupScope
import com.harmber2.suadat.ui.component.SwitchPreference
import com.harmber2.suadat.utils.isTvDevice
import com.harmber2.suadat.utils.rememberPreference
import com.harmber2.suadat.utils.resetAuthWebViewSession

val SpotifyAccountIconSize = 44.dp
const val SpotifyLoginUserAgent =
    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"

@Composable
fun SpotifyAccountIcon(avatarUrl: String?) {
    val context = LocalContext.current
    val requestSize = with(LocalDensity.current) { SpotifyAccountIconSize.roundToPx() }
    val accountIcon = painterResource(R.drawable.spotify_icon)
    val imageRequest =
        remember(context, avatarUrl, requestSize) {
            avatarUrl
                ?.takeIf(String::isNotBlank)
                ?.let {
                    ImageRequest
                        .Builder(context)
                        .data(it)
                        .size(requestSize)
                        .build()
                }
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                placeholder = accountIcon,
                error = accountIcon,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                painter = accountIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyLoginSheet(
    onDismiss: () -> Unit,
    onCookiesCaptured: (spDc: String, spKey: String) -> Unit,
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var mainWebView by remember { mutableStateOf<WebView?>(null) }
    var captured by remember { mutableStateOf(false) }
    var showManualEntry by remember { mutableStateOf(false) }
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose {
            webView?.destroySpotifyLoginWebView()
            mainWebView?.takeIf { it !== webView }?.destroySpotifyLoginWebView()
            webView = null
            mainWebView = null
        }
    }

    if (showManualEntry) {
        SpotifyManualCookieDialog(
            onDismiss = { showManualEntry = false },
            onConfirm = { spDc, spKey ->
                onCookiesCaptured(spDc, spKey)
                onDismiss()
            }
        )
    }

    BackHandler(enabled = webView != null) {
        val activeWebView = webView
        val rootWebView = mainWebView
        when {
            activeWebView?.canGoBack() == true -> {
                activeWebView.goBack()
            }

            activeWebView != null && rootWebView != null && activeWebView !== rootWebView -> {
                activeWebView.destroySpotifyLoginWebView()
                webView = rootWebView
            }

            else -> {
                onDismiss()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.IconButton(onClick = onDismiss) {
                        Icon(painterResource(R.drawable.close), contentDescription = null)
                    }
                    Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
                        Text(
                            text = stringResource(R.string.spotify_login_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.spotify_waiting_for_login),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = { showManualEntry = true }) {
                        Text("Manual Entry")
                    }
                }
                AndroidView(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    factory = { context ->
                        val container = FrameLayout(context)
                        val spotifyWebView =
                            WebView(context).apply {
                                val cookieManager = CookieManager.getInstance()
                                cookieManager.setAcceptCookie(true)
                                cookieManager.setAcceptThirdPartyCookies(this, true)
                                configureSpotifyLoginWebView()

                                fun captureCookies(url: String?): Boolean {
                                    if (captured) return true
                                    val cookies = readSpotifyCookies(cookieManager, url)
                                    val spDc = cookies["sp_dc"].orEmpty()
                                    if (spDc.isBlank()) return false
                                    captured = true
                                    cookieManager.flush()
                                    onCookiesCaptured(spDc, cookies["sp_key"].orEmpty())
                                    return true
                                }

                                webViewClient =
                                    object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            view: WebView,
                                            request: WebResourceRequest,
                                        ): Boolean =
                                            shouldOverrideSpotifyLoginUrl(
                                                view = view,
                                                url = request.url?.toString(),
                                                captureCookies = { url -> captureCookies(url) },
                                            )

                                        @Deprecated("Deprecated in Java")
                                        override fun shouldOverrideUrlLoading(
                                            view: WebView,
                                            url: String?,
                                        ): Boolean =
                                            shouldOverrideSpotifyLoginUrl(
                                                view = view,
                                                url = url,
                                                captureCookies = { targetUrl -> captureCookies(targetUrl) },
                                            )

                                        override fun onPageStarted(
                                            view: WebView,
                                            url: String?,
                                            favicon: android.graphics.Bitmap?,
                                        ) {
                                            captureCookies(url)
                                        }

                                        override fun onPageFinished(
                                            view: WebView,
                                            url: String?,
                                        ) {
                                            captureCookies(url)
                                        }
                                    }
                                webChromeClient =
                                    SpotifyLoginWebChromeClient(
                                        container = container,
                                        parentWebView = this,
                                        captureCookies = { url -> captureCookies(url) },
                                        onActiveWebViewChanged = { activeWebView -> webView = activeWebView },
                                    )
                                webView = this
                                mainWebView = this
                                resetAuthWebViewSession(context, this) {
                                    loadUrl(SpotifyAuth.LOGIN_URL)
                                }
                            }
                        container.addView(
                            spotifyWebView,
                            FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            ),
                        )
                        container
                    },
                    update = {
                        webView = webView ?: mainWebView
                    },
                )
            }
        }
    }
}

@Composable
fun SpotifyManualCookieDialog(
    onDismiss: () -> Unit,
    onConfirm: (spDc: String, spKey: String) -> Unit,
) {
    var spDc by remember { mutableStateOf("") }
    var spKey by remember { mutableStateOf("") }

    DefaultDialog(
        onDismiss = onDismiss,
        title = { Text("Manual Spotify Cookies") },
        buttons = {
            TextButton(onClick = onDismiss) { Text(stringResource(android.R.string.cancel)) }
            TextButton(
                onClick = { if (spDc.isNotBlank()) onConfirm(spDc, spKey) },
                enabled = spDc.isNotBlank()
            ) { Text(stringResource(android.R.string.ok)) }
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "Enter your Spotify session cookies below. These are usually found in your browser's developer tools under Application > Cookies.",
                style = MaterialTheme.typography.bodySmall
            )
            androidx.compose.material3.OutlinedTextField(
                value = spDc,
                onValueChange = { spDc = it },
                label = { Text("sp_dc cookie") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            androidx.compose.material3.OutlinedTextField(
                value = spKey,
                onValueChange = { spKey = it },
                label = { Text("sp_key cookie (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

fun WebView.destroySpotifyLoginWebView() {
    stopLoading()
    loadUrl("about:blank")
    (parent as? ViewGroup)?.removeView(this)
    destroy()
}

@SuppressLint("SetJavaScriptEnabled")
fun WebView.configureSpotifyLoginWebView() {
    isVerticalScrollBarEnabled = true
    isHorizontalScrollBarEnabled = false
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        javaScriptCanOpenWindowsAutomatically = true
        setSupportMultipleWindows(true)
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false
        loadWithOverviewMode = true
        useWideViewPort = true
        mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        userAgentString = SpotifyLoginUserAgent
    }
}

class SpotifyLoginWebChromeClient(
    private val container: FrameLayout,
    private val parentWebView: WebView,
    private val captureCookies: (String?) -> Boolean,
    private val onActiveWebViewChanged: (WebView) -> Unit,
) : WebChromeClient() {
    override fun onCreateWindow(
        view: WebView,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message,
    ): Boolean {
        closePopupWebViews()

        val popupWebView =
            WebView(view.context).apply {
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)
                configureSpotifyLoginWebView()
                webViewClient =
                    object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            request: WebResourceRequest,
                        ): Boolean =
                            shouldOverrideSpotifyLoginUrl(
                                view = view,
                                url = request.url?.toString(),
                                captureCookies = captureCookies,
                            )

                        @Deprecated("Deprecated in Java")
                        override fun shouldOverrideUrlLoading(
                            view: WebView,
                            url: String?,
                        ): Boolean =
                            shouldOverrideSpotifyLoginUrl(
                                view = view,
                                url = url,
                                captureCookies = captureCookies,
                            )

                        override fun onPageStarted(
                            view: WebView,
                            url: String?,
                            favicon: android.graphics.Bitmap?,
                        ) {
                            captureCookies(url)
                        }

                        override fun onPageFinished(
                            view: WebView,
                            url: String?,
                        ) {
                            captureCookies(url)
                        }
                    }
            }

        val transport = resultMsg.obj as? WebView.WebViewTransport ?: return false
        container.addView(
            popupWebView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        popupWebView.bringToFront()
        popupWebView.requestFocus()
        onActiveWebViewChanged(popupWebView)
        transport.webView = popupWebView
        resultMsg.sendToTarget()
        return true
    }

    override fun onCloseWindow(window: WebView) {
        window.destroySpotifyLoginWebView()
        onActiveWebViewChanged(parentWebView)
    }

    private fun closePopupWebViews() {
        for (index in container.childCount - 1 downTo 0) {
            val child = container.getChildAt(index) as? WebView ?: continue
            if (child !== parentWebView) {
                child.destroySpotifyLoginWebView()
            }
        }
        onActiveWebViewChanged(parentWebView)
    }
}

fun shouldOverrideSpotifyLoginUrl(
    view: WebView,
    url: String?,
    captureCookies: (String?) -> Boolean,
): Boolean {
    if (captureCookies(url)) return true

    val targetUrl = url?.takeIf(String::isNotBlank) ?: return false
    if (targetUrl.isWebViewLoadableUrl()) return false

    targetUrl.intentBrowserFallbackUrl()?.let { fallbackUrl -> view.loadUrl(fallbackUrl) }
    return true
}

fun String.isWebViewLoadableUrl(): Boolean {
    val scheme = runCatching { Uri.parse(this).scheme?.lowercase() }.getOrNull()
    return scheme == "http" ||
        scheme == "https" ||
        scheme == "javascript" ||
        scheme == "data" ||
        scheme == "blob"
}

fun String.intentBrowserFallbackUrl(): String? =
    runCatching { Intent.parseUri(this, Intent.URI_INTENT_SCHEME) }
        .getOrNull()
        ?.getStringExtra("browser_fallback_url")
        ?.takeIf { it.isWebViewLoadableUrl() }

fun readSpotifyCookies(
    cookieManager: CookieManager,
    currentUrl: String?,
): Map<String, String> {
    val urls =
        linkedSetOf(
            "https://open.spotify.com",
            "https://accounts.spotify.com",
            "https://spotify.com",
            "https://.spotify.com",
        )
    currentUrl?.toSpotifyCookieOrigin()?.let(urls::add)
    val cookies = linkedMapOf<String, String>()
    cookieManager.flush()
    urls.forEach { url ->
        cookieManager
            .getCookie(url)
            ?.split(";")
            ?.map(String::trim)
            ?.filter(String::isNotBlank)
            ?.forEach { part ->
                val separator = part.indexOf('=')
                if (separator <= 0) return@forEach
                val key = part.substring(0, separator).trim()
                val value = part.substring(separator + 1).trim()
                if (key.isNotBlank()) {
                    cookies[key] = value
                }
            }
    }
    return cookies
}

fun String.toSpotifyCookieOrigin(): String? {
    val uri = runCatching { Uri.parse(this) }.getOrNull() ?: return null
    val host = uri.host?.lowercase() ?: return null
    if (host != "spotify.com" && !host.endsWith(".spotify.com")) return null
    val scheme =
        uri.scheme
            ?.takeIf { it.equals("https", ignoreCase = true) || it.equals("http", ignoreCase = true) }
            ?: "https"
    return "$scheme://$host"
}

@Composable
fun SpotifyErrorDialog(
    message: String,
    onDismiss: () -> Unit,
) {
    DefaultDialog(
        onDismiss = onDismiss,
        title = { Text(stringResource(R.string.import_failed)) },
        buttons = {
            TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(android.R.string.ok))
            }
        },
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun PreferenceGroupScope.spotifyAccountPreferences(
    state: SpotifyAccountUiState,
    showPlaylists: Boolean,
    onConnectClick: () -> Unit,
    onShowPlaylistsChange: (Boolean) -> Unit,
    onReloadClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    if (!state.isAuthenticated) {
        item {
            PreferenceEntry(
                title = { Text(stringResource(R.string.spotify_connect)) },
                description = stringResource(R.string.spotify_not_connected),
                icon = { Icon(painterResource(R.drawable.spotify_icon), null) },
                trailingContent = {
                    AnimatedVisibility(visible = state.isLoading) {
                        CircularWavyProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                onClick = onConnectClick,
                isEnabled = !state.isLoading,
            )
        }
        return
    }

    item {
        PreferenceEntry(
            title = {
                Text(
                    text =
                        if (state.accountName.isNotBlank()) {
                            stringResource(R.string.spotify_connected_as, state.accountName)
                        } else {
                            stringResource(R.string.spotify_account)
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            description =
                when {
                    state.isLoading -> stringResource(R.string.spotify_loading_library)
                    state.playlistCount > 0 -> stringResource(R.string.spotify_available_count, state.playlistCount)
                    else -> stringResource(R.string.spotify_no_sources)
                },
            icon = { SpotifyAccountIcon(avatarUrl = state.accountAvatarUrl) },
            trailingContent = {
                AnimatedVisibility(visible = state.isLoading) {
                    CircularWavyProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            isEnabled = false,
        )
    }

    item {
        val (spotifyCanvas, onSpotifyCanvasChange) = rememberPreference(SpotifyCanvasEnabledKey, false)
        SwitchPreference(
            title = { Text(stringResource(R.string.spotify_canvas)) },
            description = stringResource(R.string.spotify_canvas_desc),
            icon = { Icon(painterResource(R.drawable.motion_photos_on), null) },
            checked = spotifyCanvas,
            onCheckedChange = onSpotifyCanvasChange,
            isEnabled = !state.isLoading,
        )
    }

    item {
        val (spotifyRecs, onSpotifyRecsChange) = rememberPreference(SpotifyRecommendationsEnabledKey, false)
        SwitchPreference(
            title = { Text(stringResource(R.string.spotify_recommendations)) },
            description = stringResource(R.string.spotify_recommendations_desc),
            icon = { Icon(painterResource(R.drawable.auto_awesome), null) },
            checked = spotifyRecs,
            onCheckedChange = onSpotifyRecsChange,
            isEnabled = !state.isLoading,
        )
    }

    item {
        SwitchPreference(
            title = { Text(stringResource(R.string.spotify_show_playlist)) },
            description = stringResource(R.string.spotify_show_playlist_desc),
            icon = { Icon(painterResource(R.drawable.spotify_icon), null) },
            checked = showPlaylists,
            onCheckedChange = onShowPlaylistsChange,
            isEnabled = !state.isLoading,
        )
    }

    item {
        PreferenceEntry(
            title = { Text(stringResource(R.string.spotify_reload_playlist)) },
            description = stringResource(R.string.spotify_reload_playlist_desc),
            icon = { Icon(painterResource(R.drawable.sync), null) },
            onClick = onReloadClick,
            isEnabled = !state.isLoading,
        )
    }

    item {
        PreferenceEntry(
            title = { Text(stringResource(R.string.action_logout)) },
            icon = { Icon(painterResource(R.drawable.logout), null) },
            onClick = onLogoutClick,
            isEnabled = !state.isLoading,
        )
    }
}
