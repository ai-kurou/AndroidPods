package kurou.androidpods.core.service

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BootReceiverTest {
    private val application: Application = ApplicationProvider.getApplicationContext()
    private val receiver = BootReceiver()

    @Test
    fun `BOOT_COMPLETEDを受信するとDeviceScanServiceが起動する`() {
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)

        receiver.onReceive(application, intent)

        val started = Shadows.shadowOf(application).nextStartedService
        assertEquals(DeviceScanService::class.java.name, started.component?.className)
    }

    @Test
    fun `BOOT_COMPLETED以外のアクションではサービスを起動しない`() {
        val intent = Intent(Intent.ACTION_USER_PRESENT)

        receiver.onReceive(application, intent)

        val started = Shadows.shadowOf(application).nextStartedService
        assertNull(started)
    }
}
