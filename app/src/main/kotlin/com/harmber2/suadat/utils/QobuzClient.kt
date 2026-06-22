/*
 * harmber (2026)
 * © Rukamori — github.com/suadatbiniqbal
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.harmber2.suadat.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.Locale
import kotlin.math.abs

@Serializable
data class QobuzResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
)

@Serializable
data class QobuzSearchData(
    val tracks: QobuzTrackList,
)

@Serializable
data class QobuzTrackList(
    val items: List<QobuzTrack>,
)

@Serializable
data class QobuzTrack(
    val id: Long,
    val title: String,
    val duration: Int,
    val performer: QobuzPerformer,
    val album: QobuzAlbum,
    @SerialName("maximum_bit_depth") val maximumBitDepth: Int,
    @SerialName("maximum_sampling_rate") val maximumSamplingRate: Float,
    val streamable: Boolean,
)

@Serializable
data class QobuzPerformer(
    val name: String,
)

@Serializable
data class QobuzAlbum(
    val title: String,
    val image: QobuzImage,
)

@Serializable
data class QobuzImage(
    val large: String,
)

@Serializable
data class QobuzDownloadData(
    val url: String,
)

class QobuzApiException(val status: Int, message: String) : Exception(message)

object QobuzClient {
    private const val BASE_URL = "https://qobuz.kennyy.com.br/api"
    private const val USER_AGENT = "Harmber/1.0"

    private val json =
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
        }

    private val client = OkHttpClient()

    suspend fun searchTracks(query: String): List<QobuzTrack> =
        withContext(Dispatchers.IO) {
            val url = "$BASE_URL/get-music?q=${java.net.URLEncoder.encode(query, "UTF-8")}&offset=0"
            val request =
                Request
                    .Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw QobuzApiException(response.code, "Failed to search tracks: ${response.message}")
                    }
                    val body = response.body.string()
                    val qobuzResponse = json.decodeFromString<QobuzResponse<QobuzSearchData>>(body)
                    if (!qobuzResponse.success) {
                        throw QobuzApiException(response.code, qobuzResponse.error ?: "Unknown error")
                    }
                    qobuzResponse.data?.tracks?.items ?: emptyList()
                }
            } catch (e: Exception) {
                Timber.e(e, "Qobuz search failed")
                throw e
            }
        }

    suspend fun getStreamUrl(
        trackId: Long,
        quality: Int = 27,
    ): String =
        withContext(Dispatchers.IO) {
            val url = "$BASE_URL/download-music?track_id=$trackId&quality=$quality"
            val request =
                Request
                    .Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw QobuzApiException(response.code, "Failed to get download URL: ${response.message}")
                    }
                    val body = response.body.string()
                    val qobuzResponse = json.decodeFromString<QobuzResponse<QobuzDownloadData>>(body)
                    if (!qobuzResponse.success) {
                        throw QobuzApiException(response.code, qobuzResponse.error ?: "Unknown error")
                    }
                    qobuzResponse.data?.url ?: throw QobuzApiException(response.code, "URL not found in response")
                }
            } catch (e: Exception) {
                Timber.e(e, "Qobuz download URL failed")
                throw e
            }
        }

    fun computeConfidence(
        track: QobuzTrack,
        targetTitle: String,
        targetArtist: String,
        targetDurationSeconds: Int,
    ): Double {
        val titleSim = jaccardSimilarity(normalize(track.title), normalize(targetTitle))
        val artistSim = artistSimilarity(track.performer.name, targetArtist)

        val durationDrift = abs(track.duration - targetDurationSeconds).toDouble() / targetDurationSeconds
        val durationFactor =
            when {
                durationDrift <= 0.05 -> 1.0
                durationDrift <= 0.10 -> 0.85
                durationDrift <= 0.20 -> 0.6
                else -> 0.3
            }

        return titleSim * artistSim * durationFactor
    }

    private fun normalize(text: String): Set<String> {
        return text
            .lowercase(Locale.getDefault())
            .replace(Regex("""\(feat\..*?\)|\(ft\..*?\)|feat\..*?|ft\..*?"""), "")
            .replace(Regex("""\[.*?]|\(.*?\)"""), "")
            .replace(Regex("""[^\w\s]"""), "")
            .split(Regex("""\s+"""))
            .filter { it.isNotBlank() }
            .toSet()
    }

    private fun jaccardSimilarity(
        s1: Set<String>,
        s2: Set<String>,
    ): Double {
        if (s1.isEmpty() && s2.isEmpty()) return 1.0
        val intersection = s1.intersect(s2).size
        val union = s1.union(s2).size
        return intersection.toDouble() / union
    }

    private fun artistSimilarity(
        qobuzArtist: String,
        targetArtist: String,
    ): Double {
        val s1 = normalize(qobuzArtist)
        val s2 = normalize(targetArtist)

        val jaccard = jaccardSimilarity(s1, s2)

        // Full coverage score
        val smaller = if (s1.size < s2.size) s1 else s2
        val larger = if (s1.size < s2.size) s2 else s1

        if (smaller.isNotEmpty() && larger.containsAll(smaller)) {
            // Check for distinctive token (arbitrarily length > 2)
            if (smaller.any { it.length > 2 }) {
                return 1.0
            }
        }

        return jaccard
    }

    suspend fun findBestMatch(
        title: String,
        artist: String,
        durationSeconds: Int,
    ): QobuzTrack? {
        val primaryArtist = artist.split(",").first().trim()

        val queries =
            listOf(
                "$artist $title",
                "${normalize(artist).joinToString(" ")} ${normalize(title).joinToString(" ")}",
                "$primaryArtist $title",
            ).distinct()

        val allCandidates = mutableListOf<QobuzTrack>()
        for (query in queries) {
            try {
                allCandidates.addAll(searchTracks(query))
            } catch (e: Exception) {
                Timber.w(e, "Search failed for query: $query")
            }
        }

        return allCandidates
            .distinctBy { it.id }
            .filter { it.streamable && it.maximumBitDepth >= 16 }
            .map { it to computeConfidence(it, title, artist, durationSeconds) }
            .filter { it.second >= 0.5 }
            .maxByOrNull { it.second }
            ?.first
    }
}
