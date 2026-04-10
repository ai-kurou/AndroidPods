package kurou.androidpods.core.domain

import android.bluetooth.BluetoothAdapter
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetBluetoothAdapterStateUseCaseTest {

    private lateinit var useCase: GetBluetoothAdapterStateUseCase
    private val repository = mockk<BluetoothAdapterRepository>()

    @Before
    fun setUp() {
        useCase = GetBluetoothAdapterStateUseCase(repository)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `observeがrepositoryのobserveAdapterStateのFlowを返す`() = runTest {
        val fakeFlow = MutableStateFlow<Int?>(BluetoothAdapter.STATE_ON)
        every { repository.observeAdapterState() } returns fakeFlow

        val result = useCase.observe().first()

        assertEquals(BluetoothAdapter.STATE_ON, result)
        verify(exactly = 1) { repository.observeAdapterState() }
        confirmVerified(repository)
    }

    @Test
    fun `currentがrepositoryのgetCurrentStateの値を返す`() {
        every { repository.getCurrentState() } returns BluetoothAdapter.STATE_ON

        val result = useCase.current()

        assertEquals(BluetoothAdapter.STATE_ON, result)
        verify(exactly = 1) { repository.getCurrentState() }
        confirmVerified(repository)
    }
}
