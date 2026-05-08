package kurou.androidpods.core.designsystem

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorTest {
    @Test
    fun `BlueGrey80の色値が正しい`() {
        assertEquals(Color(0xFF81D3DB), BlueGrey80)
    }

    @Test
    fun `BlueGreyNeutral80の色値が正しい`() {
        assertEquals(Color(0xFFA5C8CE), BlueGreyNeutral80)
    }

    @Test
    fun `CoolGrey80の色値が正しい`() {
        assertEquals(Color(0xFFB9C8CC), CoolGrey80)
    }

    @Test
    fun `BlueGrey40の色値が正しい`() {
        assertEquals(Color(0xFF4A6268), BlueGrey40)
    }

    @Test
    fun `BlueGreyNeutral40の色値が正しい`() {
        assertEquals(Color(0xFF525E62), BlueGreyNeutral40)
    }

    @Test
    fun `CoolGrey40の色値が正しい`() {
        assertEquals(Color(0xFF616971), CoolGrey40)
    }
}
