package kurou.androidpods.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ParseProximityPairingDataTest {

    private val testAddress = "AA:BB:CC:DD:EE:FF"
    private val testRssi = -45

    private fun buildData(
        type: Byte = 0x07,
        length: Byte = 0x19,
        modelHigh: Byte = 0x14,
        modelLow: Byte = 0x20,
        byte5: Byte = 0x00,
        byte6: Byte = 0x99.toByte(),
        byte7: Byte = 0x07,
    ): ByteArray = ByteArray(27).apply {
        this[0] = type
        this[1] = length
        this[3] = modelHigh
        this[4] = modelLow
        this[5] = byte5
        this[6] = byte6
        this[7] = byte7
    }

    @Test
    fun `サイズが27でないとnullを返す`() {
        assertNull(parseProximityPairingData(ByteArray(26), testAddress, testRssi))
        assertNull(parseProximityPairingData(ByteArray(28), testAddress, testRssi))
    }

    @Test
    fun `タイプバイトが0x07でないとnullを返す`() {
        val data = buildData(type = 0x01)
        assertNull(parseProximityPairingData(data, testAddress, testRssi))
    }

    @Test
    fun `正常なTWSデータをパースできる`() {
        // AirPods Pro 2nd Gen (0x1420), byte6=0x98 → upper=9, lower=8, byte7=0x07 → case=7
        val data = buildData(byte6 = 0x98.toByte(), byte7 = 0x07)
        val device = parseProximityPairingData(data, testAddress, testRssi)

        assertNotNull(device)
        device!!
        assertEquals("AirPods Pro (2nd Gen)", device.modelName)
        assertEquals(0x1420, device.modelCode)
        assertEquals(testAddress, device.address)
        assertEquals(testRssi, device.rssi)
        assertEquals(7, device.caseBattery)
        assertFalse(device.isSingle)
    }

    @Test
    fun `0x0Fはバッテリー未接続としてnullになる`() {
        // byte6=0xFF → upper=F, lower=F → 両方null
        val data = buildData(byte6 = 0xFF.toByte(), byte7 = 0x0F)
        val device = parseProximityPairingData(data, testAddress, testRssi)

        assertNotNull(device)
        device!!
        assertNull(device.leftBattery)
        assertNull(device.rightBattery)
        assertNull(device.caseBattery)
    }

    @Test
    fun `フリップビットでL_Rが入れ替わる`() {
        // byte5=0x00 → upper nibble=0, (0 & 0x02)==0 → flipped=true
        // byte6=0xF9 → upper=F, lower=9
        // flipped=true → left=upper=F(null), right=lower=9
        val flipped = buildData(byte5 = 0x00, byte6 = 0xF9.toByte())
        val deviceFlipped = parseProximityPairingData(flipped, testAddress, testRssi)!!
        assertEquals(0x0F, (0xF9 shr 4) and 0x0F) // upper=F
        assertNull(deviceFlipped.leftBattery) // upper=F → null
        assertEquals(9, deviceFlipped.rightBattery) // lower=9

        // byte5=0x20 → upper nibble=2, (2 & 0x02)!=0 → flipped=false
        // byte6=0xF9 → upper=F, lower=9
        // flipped=false → left=lower=9, right=upper=F(null)
        val notFlipped = buildData(byte5 = 0x20, byte6 = 0xF9.toByte())
        val deviceNotFlipped = parseProximityPairingData(notFlipped, testAddress, testRssi)!!
        assertEquals(9, deviceNotFlipped.leftBattery) // lower=9
        assertNull(deviceNotFlipped.rightBattery) // upper=F → null
    }

    @Test
    fun `ケースバッテリーはbyte7の下位ニブル`() {
        // byte7=0x85 → 下位ニブル=5
        val data = buildData(byte7 = 0x85.toByte())
        val device = parseProximityPairingData(data, testAddress, testRssi)!!
        assertEquals(5, device.caseBattery)
    }

    @Test
    fun `シングルデバイスはbyte6の下位ニブルのみ使用`() {
        // Beats Flex (0x1020), byte6=0xF9 → lower=9
        val data = buildData(modelHigh = 0x10, modelLow = 0x20, byte6 = 0xF9.toByte())
        val device = parseProximityPairingData(data, testAddress, testRssi)

        assertNotNull(device)
        device!!
        assertEquals("Beats Flex", device.modelName)
        assertTrue(device.isSingle)
        assertEquals(9, device.leftBattery)
        assertNull(device.rightBattery)
        assertNull(device.caseBattery)
    }

    @Test
    fun `シングルデバイスで0x0Fのときバッテリーがnull`() {
        val data = buildData(modelHigh = 0x10, modelLow = 0x20, byte6 = 0xFF.toByte())
        val device = parseProximityPairingData(data, testAddress, testRssi)!!

        assertTrue(device.isSingle)
        assertNull(device.leftBattery)
    }

    @Test
    fun `未知のモデルコードはTWSとして扱う`() {
        val data = buildData(modelHigh = 0x00, modelLow = 0x01)
        val device = parseProximityPairingData(data, testAddress, testRssi)!!

        assertFalse(device.isSingle)
        assertTrue(device.modelName.startsWith("Unknown Apple Device"))
    }
}
