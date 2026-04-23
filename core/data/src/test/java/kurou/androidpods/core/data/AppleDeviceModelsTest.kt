package kurou.androidpods.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppleDeviceModelsTest {

    @Test
    fun `appleModelName - 既知のモデルコードは正しい名前を返す`() {
        assertEquals("AirPods Pro (2nd Gen)", appleModelName(0x1420))
    }

    @Test
    fun `appleModelName - 未知のモデルコードはUnknown文字列とhex表記を返す`() {
        val result = appleModelName(0x9999)
        assertTrue(result.startsWith("Unknown Apple Device (0x9999)"))
    }
}
