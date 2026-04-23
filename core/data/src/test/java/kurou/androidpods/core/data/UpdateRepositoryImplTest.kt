package kurou.androidpods.core.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateRepositoryImplTest {
    private fun buildClient(engine: MockEngine): HttpClient =
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

    @Test
    fun `最新タグ名を正常に取得できる`() =
        runTest {
            val engine =
                MockEngine { _ ->
                    respond(
                        content = """{"tag_name": "v1.2.3", "name": "Release 1.2.3"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf("Content-Type", ContentType.Application.Json.toString()),
                    )
                }
            val repository = UpdateRepositoryImpl(buildClient(engine))

            val result = repository.fetchLatestTagName()

            assertEquals("v1.2.3", result)
        }

    @Test
    fun `HTTPエラー時はnullを返す`() =
        runTest {
            val engine =
                MockEngine { _ ->
                    respond(
                        content = "",
                        status = HttpStatusCode.NotFound,
                    )
                }
            val repository = UpdateRepositoryImpl(buildClient(engine))

            val result = repository.fetchLatestTagName()

            assertNull(result)
        }

    @Test
    fun `ネットワークエラー時はnullを返す`() =
        runTest {
            val engine =
                MockEngine { _ ->
                    throw Exception("network error")
                }
            val repository = UpdateRepositoryImpl(buildClient(engine))

            val result = repository.fetchLatestTagName()

            assertNull(result)
        }
}
