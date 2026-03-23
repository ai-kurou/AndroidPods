package kurou.androidpods.feature.devices

import android.Manifest
import android.os.Build
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class BluetoothPermissionsTest {

    @Config(sdk = [Build.VERSION_CODES.S])
    @Test
    fun `Android 12以上ではBLUETOOTH_CONNECTとBLUETOOTH_SCANを要求する`() {
        val permissions = requiredPermissions()

        assertEquals(2, permissions.size)
        assertTrue(permissions.contains(Manifest.permission.BLUETOOTH_CONNECT))
        assertTrue(permissions.contains(Manifest.permission.BLUETOOTH_SCAN))
    }

    @Config(sdk = [Build.VERSION_CODES.R])
    @Test
    fun `Android 11以下ではACCESS_FINE_LOCATIONを要求する`() {
        val permissions = requiredPermissions()

        assertEquals(1, permissions.size)
        assertTrue(permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION))
    }
}
