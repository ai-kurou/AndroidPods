package kurou.androidpods.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorTest {
    @Test
    fun `Purple80の色値が正しい`() {
        assertEquals(Color(0xFFD0BCFF), Purple80)
    }

    @Test
    fun `PurpleGrey80の色値が正しい`() {
        assertEquals(Color(0xFFCCC2DC), PurpleGrey80)
    }

    @Test
    fun `Pink80の色値が正しい`() {
        assertEquals(Color(0xFFEFB8C8), Pink80)
    }

    @Test
    fun `Purple40の色値が正しい`() {
        assertEquals(Color(0xFF6650a4), Purple40)
    }

    @Test
    fun `PurpleGrey40の色値が正しい`() {
        assertEquals(Color(0xFF625b71), PurpleGrey40)
    }

    @Test
    fun `Pink40の色値が正しい`() {
        assertEquals(Color(0xFF7D5260), Pink40)
    }
}
