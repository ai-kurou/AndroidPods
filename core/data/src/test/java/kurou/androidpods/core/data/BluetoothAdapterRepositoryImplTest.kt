package kurou.androidpods.core.data

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BluetoothAdapterRepositoryImplTest {

    private val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    private val repository = BluetoothAdapterRepositoryImpl(context)

    @Test
    fun `getCurrentStateでアダプタの現在の状態を取得できる`() {
        val state = repository.getCurrentState()

        assertNotNull(state)
    }

    @Test
    fun `STATE_CHANGEDブロードキャストを受信すると状態が順番に流れる`() = runTest {
        val results = mutableListOf<Int?>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.observeAdapterState().take(3).toList(results)
        }

        sendBluetoothStateChanged(BluetoothAdapter.STATE_TURNING_OFF)
        sendBluetoothStateChanged(BluetoothAdapter.STATE_OFF)

        job.join()
        assertEquals(3, results.size)
        assertEquals(BluetoothAdapter.STATE_DISCONNECTED, results[0])
        assertEquals(BluetoothAdapter.STATE_TURNING_OFF, results[1])
        assertEquals(BluetoothAdapter.STATE_OFF, results[2])
    }

    private fun sendBluetoothStateChanged(state: Int) {
        val intent = Intent(BluetoothAdapter.ACTION_STATE_CHANGED).apply {
            putExtra(BluetoothAdapter.EXTRA_STATE, state)
        }
        context.sendBroadcast(intent)
        ShadowLooper.idleMainLooper()
    }
}
