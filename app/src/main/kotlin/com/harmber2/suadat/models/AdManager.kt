/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.models

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

object AdManager {
    val ads = MutableStateFlow<List<BannerAd>>(emptyList())
    val config = MutableStateFlow(AdConfig())
    private var isObserving = false

    fun observeAds() {
        if (isObserving) return
        isObserving = true
        
        try {
            val database = FirebaseDatabase.getInstance()
            
            // Observe Ads
            database.getReference("banner_ads").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val adList = mutableListOf<BannerAd>()
                    for (adSnapshot in snapshot.children) {
                        adSnapshot.getValue(BannerAd::class.java)?.let {
                            if (it.active) adList.add(it)
                        }
                    }
                    ads.value = adList.sortedByDescending { it.priority }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            // Observe Config
            database.getReference("ad_config").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.getValue(AdConfig::class.java)?.let {
                        config.value = it
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: Exception) {
            Timber.tag("AdManager").e(e, "Error initializing ads observer")
        }
    }
}
