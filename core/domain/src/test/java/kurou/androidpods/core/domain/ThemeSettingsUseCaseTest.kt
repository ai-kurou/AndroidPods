package kurou.androidpods.core.domain

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ThemeSettingsUseCaseTest {
    private lateinit var useCase: ThemeSettingsUseCase
    private val repository = mockk<ThemeSettingsRepository>(relaxUnitFun = true)

    @Before
    fun setUp() {
        useCase = ThemeSettingsUseCase(repository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `observeがrepositoryのobserveのFlowを返す`() =
        runTest {
            val expected = ThemeSettings(ThemeMode.DARK, useDynamicColor = false)
            val fakeFlow = MutableStateFlow(expected)
            every { repository.observe() } returns fakeFlow

            val result = useCase.observe().first()

            assertEquals(expected, result)
        }

    @Test
    fun `updateでrepositoryのupdateが呼ばれる`() =
        runTest {
            val settings = ThemeSettings(ThemeMode.LIGHT, useDynamicColor = true)

            useCase.update(settings)

            coVerify(exactly = 1) { repository.update(settings) }
            confirmVerified(repository)
        }
}
