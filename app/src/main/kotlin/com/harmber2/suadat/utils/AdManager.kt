/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.utils

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import timber.log.Timber

object AdManager {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    // Rewarded Ad ID
    private const val AD_UNIT_ID = "ca-app-pub-4253536927389825/3594343220"

    fun initialize(context: Context) {
        MobileAds.initialize(context) {
            Timber.d("AdMob initialized")
            loadRewardedAd(context)
        }
    }

    fun loadRewardedAd(context: Context) {
        if (isLoading || rewardedAd != null) return

        isLoading = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(context, AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Timber.e("Ad failed to load: ${adError.message}")
                rewardedAd = null
                isLoading = false
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Timber.d("Ad loaded")
                rewardedAd = ad
                isLoading = false
            }
        })
    }

    fun showRewardedAd(activity: Activity, onRewarded: () -> Unit) {
        rewardedAd?.let { ad ->
            ad.show(activity) { _ ->
                onRewarded()
            }
            rewardedAd = null
            loadRewardedAd(activity)
        } ?: run {
            Timber.d("Ad not ready yet")
            loadRewardedAd(activity)
        }
    }

    fun isAdLoaded(): Boolean = rewardedAd != null
}
