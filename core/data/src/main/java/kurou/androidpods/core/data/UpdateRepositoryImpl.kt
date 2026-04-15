package kurou.androidpods.core.data

import kurou.androidpods.core.domain.UpdateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class UpdateRepositoryImpl @Inject constructor() : UpdateRepository {

    companion object {
        private const val API_URL =
            "https://api.github.com/repos/ai-kurou/AndroidPods/releases/latest"
        private const val TIMEOUT_MS = 5_000
    }

    override suspend fun fetchLatestTagName(): String? = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(API_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("Accept", "application/vnd.github+json")
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()
            JSONObject(body).getString("tag_name")
        } catch (e: Exception) {
            null
        }
    }
}
