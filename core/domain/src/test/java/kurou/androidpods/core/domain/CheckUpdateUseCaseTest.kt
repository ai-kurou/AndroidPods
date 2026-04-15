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
    fun `最新バージョンが現在より大きい場合はtrueを返す`() = runTest {
        coEvery { repository.fetchLatestTagName() } returns "v1.0.0"

        assertTrue(useCase("0.1.0"))
    }

    @Test
    fun `マイナーバージョンが現在より大きい場合はtrueを返す`() = runTest {
        coEvery { repository.fetchLatestTagName() } returns "v0.2.0"

        assertTrue(useCase("0.1.9"))
    }

    @Test
    fun `パッチバージョンが現在より大きい場合はtrueを返す`() = runTest {
        coEvery { repository.fetchLatestTagName() } returns "v0.1.1"

        assertTrue(useCase("0.1.0"))
    }

    @Test
    fun `同バージョンの場合はfalseを返す`() = runTest {
        coEvery { repository.fetchLatestTagName() } returns "v0.1.0"

        assertFalse(useCase("0.1.0"))
    }

    @Test
    fun `最新バージョンが現在より古い場合はfalseを返す`() = runTest {
        coEvery { repository.fetchLatestTagName() } returns "v0.0.9"

        assertFalse(useCase("0.1.0"))
    }

    @Test
    fun `repositoryがnullを返す場合はfalseを返す`() = runTest {
        coEvery { repository.fetchLatestTagName() } returns null

        assertFalse(useCase("0.1.0"))
    }

    @Test
    fun `tag_nameにvプレフィックスがない場合も正しく比較できる`() = runTest {
        coEvery { repository.fetchLatestTagName() } returns "1.0.0"

        assertTrue(useCase("0.1.0"))
    }

    @Test
    fun `バージョン文字列が不正な場合はfalseを返す`() = runTest {
        coEvery { repository.fetchLatestTagName() } returns "vINVALID"

        assertFalse(useCase("0.1.0"))
    }

    @Test
    fun `最新バージョンのコンポーネント数が多い場合に正しく比較できる`() = runTest {
        // l="1.0.1"(3コンポーネント) vs c="1.0"(2コンポーネント): c.getOrElse(2){0}が呼ばれる
        coEvery { repository.fetchLatestTagName() } returns "v1.0.1"

        assertTrue(useCase("1.0"))
    }

    @Test
    fun `現在バージョンのコンポーネント数が多い場合に正しく比較できる`() = runTest {
        // l="1.0"(2コンポーネント) vs c="1.0.1"(3コンポーネント): l.getOrElse(2){0}が呼ばれる
        coEvery { repository.fetchLatestTagName() } returns "v1.0"

        assertFalse(useCase("1.0.1"))
    }
}
