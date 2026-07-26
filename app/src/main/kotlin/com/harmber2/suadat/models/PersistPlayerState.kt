/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.models

import java.io.Serializable

data class PersistPlayerState(
    val playWhenReady: Boolean = false,
    val repeatMode: Int = 0,
    val shuffleModeEnabled: Boolean = false,
    val volume: Float = 1f,
    val currentPosition: Long = 0L,
    val currentMediaItemIndex: Int = 0,
    val playbackState: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
