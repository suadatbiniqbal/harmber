/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.playback.qobuz

import com.harmber2.suadat.innertube.models.response.PlayerResponse
import com.harmber2.suadat.utils.YTPlayerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object QobuzManager {
    private const val BASE_URL = "https://qobuz.kennyy.com.br/api"
    private const val QOBUZ_ITAG = -7003
    private const val USER_AGENT = "Harmber/1.0"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun tryQobuz(title: String, artist: String, durationSeconds: Int?): YTPlayerUtils.PlaybackData? = withContext(Dispatchers.IO) {
        repeat(3) { attempt ->
            try {
                val track = searchAndMatch(title, artist, durationSeconds) ?: return@repeat
                val streamUrl = getStreamUrl(track.id) ?: return@repeat
                
                return@withContext YTPlayerUtils.PlaybackData(
                    audioConfig = null,
                    videoDetails = null,
                    playbackTracking = null,
                    format = PlayerResponse.StreamingData.Format(
                        itag = QOBUZ_ITAG,
                        url = streamUrl,
                        mimeType = "audio/flac; codecs=\"flac\"",
                        bitrate = (track.maximum_sampling_rate * 1000 * track.maximum_bit_depth * 2).toInt(),
                        quality = "LOSSLESS",
                        audioQuality = "LOSSLESS",
                        approxDurationMs = (track.duration * 1000L).toString()
                    ),
                    streamUrl = streamUrl,
                    streamExpiresInSeconds = 3600,
                    authFingerprint = "external:qobuz"
                )
            } catch (e: Exception) {
                Timber.e(e, "Qobuz attempt ${attempt + 1} failed")
                if (attempt < 2) delay(1.seconds)
            }
        }
        null
    }

    private fun searchAndMatch(title: String, artist: String, durationSeconds: Int?): QobuzTrack? {
        val queries = buildSearchQueries(title, artist)
        val candidates = mutableListOf<Pair<QobuzTrack, Double>>()

        for (query in queries) {
            val results = search(query)
            for (track in results) {
                if (!track.streamable || track.maximum_bit_depth < 16) continue
                
                val confidence = computeConfidence(track, title, artist, durationSeconds)
                if (confidence >= 0.5) {
                    candidates.add(track to confidence)
                }
            }
            if (candidates.isNotEmpty()) break
        }

        return candidates.maxByOrNull { it.second }?.first
    }

    private fun buildSearchQueries(title: String, artist: String): List<String> {
        val cleanedTitle = title.replace(Regex("""\(feat\..*?\)|\(ft\..*?\)|\[.*?]|Official Video|Official Music Video""", RegexOption.IGNORE_CASE), "").trim()
        val primaryArtist = artist.split(",").first().trim()

        return listOf(
            "$artist $title",
            "$artist $cleanedTitle",
            "$primaryArtist $cleanedTitle"
        ).distinct()
    }

    private fun search(query: String): List<QobuzTrack> {
        val url = "$BASE_URL/get-music?q=${java.net.URLEncoder.encode(query, "UTF-8")}&offset=0"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body.string()
                val qobuzResponse = json.decodeFromString<QobuzResponse<QobuzSearchData>>(body)
                qobuzResponse.data?.tracks?.items ?: emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Qobuz search failed")
            emptyList()
        }
    }

    private fun getStreamUrl(trackId: Long): String? {
        val url = "$BASE_URL/download-music?track_id=$trackId&quality=27"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body.string()
                val qobuzResponse = json.decodeFromString<QobuzResponse<QobuzDownloadData>>(body)
                qobuzResponse.data?.url
            }
        } catch (e: Exception) {
            Timber.e(e, "Qobuz download failed")
            null
        }
    }

    private fun computeConfidence(track: QobuzTrack, targetTitle: String, targetArtist: String, targetDuration: Int?): Double {
        val titleSim = jaccardSimilarity(normalize(track.title), normalize(targetTitle))
        val artistSim = artistSimilarity(track.performer.name, targetArtist)
        val durationFactor = if (targetDuration != null && targetDuration > 0) {
            val drift = abs(track.duration - targetDuration).toDouble() / targetDuration
            when {
                drift <= 0.05 -> 1.0
                drift <= 0.10 -> 0.85
                drift <= 0.20 -> 0.6
                else -> 0.3
            }
        } else 1.0

        return titleSim * artistSim * durationFactor
    }

    private fun normalize(text: String): Set<String> {
        return text.lowercase()
            .replace(Regex("""\(feat\..*?\)|\(ft\..*?\)|\[.*?]""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""[^\p{L}\p{N}\s]"""), "")
            .split(Regex("""\s+"""))
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun jaccardSimilarity(s1: Set<String>, s2: Set<String>): Double {
        if (s1.isEmpty() && s2.isEmpty()) return 1.0
        val intersect = s1.intersect(s2).size
        val union = s1.union(s2).size
        return intersect.toDouble() / union
    }

    private fun artistSimilarity(a1: String, a2: String): Double {
        val s1 = normalize(a1)
        val s2 = normalize(a2)
        val jaccard = jaccardSimilarity(s1, s2)
        
        val smaller = if (s1.size < s2.size) s1 else s2
        val larger = if (s1.size < s2.size) s2 else s1
        
        if (smaller.isNotEmpty() && larger.containsAll(smaller)) {
            return 1.0
        }
        
        return jaccard
    }
}
