package kurou.androidpods.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.text.contains

class CompatibleDeviceRepositoryImplTest {
    private val repository = CompatibleDeviceRepositoryImpl()

    @Test
    fun `getCompatibleDevicesはAPPLE_DEVICE_MODELSを返す`() {
        val result = repository.getCompatibleDevices()
        val resultNames = result.map { it.name }.toSet()
        val missingImages = result.filter { it.images == null }.map { it.name }

        assertEquals(APPLE_DEVICE_MODELS.size, result.size)
        assertTrue(APPLE_DEVICE_MODELS.values.all { it in resultNames })
        assertTrue("画像未定義のモデル: $missingImages", missingImages.isEmpty())
    }
}
