/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat

import com.harmber2.suadat.BuildConfig

import com.harmber2.suadat.constants.UpdateChannel

private val DailyNightlyVersionRegex = Regex("""^N\d{8}$""")

internal val isDailyNightlyBuild: Boolean
    get() = DailyNightlyVersionRegex.matches(BuildConfig.VERSION_NAME)

internal val defaultUpdateChannel: UpdateChannel
    get() = UpdateChannel.STABLE

internal val currentBuildHash: String?
    get() = BuildConfig.NIGHTLY_BUILD_HASH.takeIf { it.isNotBlank() }

internal fun formatVersionName(
    versionName: String = BuildConfig.VERSION_NAME,
    buildHash: String? = currentBuildHash,
): String = listOfNotNull(versionName.takeIf { it.isNotBlank() }, buildHash).joinToString(" ")
