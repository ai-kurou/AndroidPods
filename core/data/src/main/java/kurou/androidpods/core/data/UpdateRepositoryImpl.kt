package kurou.androidpods.core.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kurou.androidpods.core.domain.UpdateRepository
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
internal data class GitHubRelease(
    @SerialName("tag_name") val tagName: String,
)

@Singleton
internal class UpdateRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
) : UpdateRepository {
    companion object {
        private const val API_URL =
            "https://api.github.com/repos/ai-kurou/AndroidPods/releases/latest"
    }

    override suspend fun fetchLatestTagName(): String? =
        try {
            httpClient
                .get(API_URL) {
                    header("Accept", "application/vnd.github+json")
                }.body<GitHubRelease>()
                .tagName
        } catch (e: Exception) {
            null
        }
}
