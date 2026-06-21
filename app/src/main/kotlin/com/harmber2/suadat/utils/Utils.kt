/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import java.util.Locale

fun reportException(throwable: Throwable) {
    throwable.printStackTrace()
}

@Suppress("DEPRECATION")
fun setAppLocale(
    context: Context,
    locale: Locale,
) {
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}

fun Context.isTvDevice(): Boolean {
    val isTelevisionUiMode =
        (this.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) ==
            Configuration.UI_MODE_TYPE_TELEVISION
    return isTelevisionUiMode ||
        this.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
        this.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEVISION)
}
