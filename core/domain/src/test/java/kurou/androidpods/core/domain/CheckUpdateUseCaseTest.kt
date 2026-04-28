package kurou.androidpods.core.domain

import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckUpdateUseCaseTest {
    private lateinit var useCase: CheckUpdateUseCase
    private val repository = mockk<UpdateRepository>()

    @Before
    fun setUp() {
        useCase = CheckUpdateUseCase(repository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `最新バージョンが現在より大きい場合はResult_success_trueを返す`() =
        runTest {
            coEvery { repository.fetchLatestTagName() } returns Result.success("v1.0.0")

            assertTrue(useCase("0.1.0").getOrThrow())
        }

    @Test
    fun `マイナーバージョンが現在より大きい場合はResult_success_trueを返す`() =
        runTest {
            coEvery { repository.fetchLatestTagName() } returns Result.success("v0.2.0")

            assertTrue(useCase("0.1.9").getOrThrow())
        }

    @Test
    fun `パッチバージョンが現在より大きい場合はResult_success_trueを返す`() =
        runTest {
            coEvery { repository.fetchLatestTagName() } returns Result.success("v0.1.1")

            assertTrue(useCase("0.1.0").getOrThrow())
        }

    @Test
    fun `同バージョンの場合はResult_success_falseを返す`() =
        runTest {
            coEvery { repository.fetchLatestTagName() } returns Result.success("v0.1.0")

            assertFalse(useCase("0.1.0").getOrThrow())
        }

    @Test
    fun `最新バージョンが現在より古い場合はResult_success_falseを返す`() =
        runTest {
            coEvery { repository.fetchLatestTagName() } returns Result.success("v0.0.9")

            assertFalse(useCase("0.1.0").getOrThrow())
        }

    @Test
    fun `repositoryがResult_failureを返す場合はResult_failureを返す`() =
        runTest {
            coEvery { repository.fetchLatestTagName() } returns Result.failure(Exception("network error"))

            assertTrue(useCase("0.1.0").isFailure)
        }

    @Test
    fun `tag_nameにvプレフィックスがない場合も正しく比較できる`() =
        runTest {
            coEvery { repository.fetchLatestTagName() } returns Result.success("1.0.0")

            assertTrue(useCase("0.1.0").getOrThrow())
        }

    @Test
    fun `バージョン文字列が不正な場合はResult_failureを返す`() =
        runTest {
            coEvery { repository.fetchLatestTagName() } returns Result.success("vINVALID")

            assertTrue(useCase("0.1.0").isFailure)
        }
}
