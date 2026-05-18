package kurou.androidpods.core.domain

import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetOverlaySettingsUseCaseTest {
    private lateinit var useCase: GetOverlaySettingsUseCase
    private val repository = mockk<OverlaySettingsRepository>(relaxUnitFun = true)

    @Before
    fun setUp() {
        useCase = GetOverlaySettingsUseCase(repository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `observeがrepositoryのobserveのFlowを返す`() =
        runTest {
            val fakeFlow = MutableStateFlow(true)
            every { repository.observe() } returns fakeFlow

            val result = useCase.observe().first()

            assertTrue(result)
            verify(exactly = 1) { repository.observe() }
            confirmVerified(repository)
        }

    @Test
    fun `hasPermissionがrepositoryのhasPermissionを返す`() {
        every { repository.hasPermission() } returns false

        val result = useCase.hasPermission()

        assertFalse(result)
        verify(exactly = 1) { repository.hasPermission() }
        confirmVerified(repository)
    }

    @Test
    fun `setEnabledがrepositoryのsetEnabledを呼ぶ`() =
        runTest {
            useCase.setEnabled(true)

            coVerify(exactly = 1) { repository.setEnabled(true) }
            confirmVerified(repository)
        }

    @Test
    fun `refreshがrepositoryのrefreshを呼ぶ`() {
        useCase.refresh()

        verify(exactly = 1) { repository.refresh() }
        confirmVerified(repository)
    }
}
