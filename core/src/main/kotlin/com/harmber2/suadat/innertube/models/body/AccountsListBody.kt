/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.innertube.models.body

import kotlinx.serialization.Serializable
import com.harmber2.suadat.innertube.models.Context

@Serializable
data class AccountsListBody(
    val context: Context,
    val requestType: String = "ACCOUNTS_LIST_REQUEST_TYPE_CHANNEL_SWITCHER",
    val callCircumstance: String = "SWITCHING_USERS_FULL",
)
