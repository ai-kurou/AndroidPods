package kurou.androidpods.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class TypeTest {
    @Test
    fun `bodyLargeのフォントファミリーがDefaultである`() {
        assertEquals(FontFamily.Default, Typography.bodyLarge.fontFamily)
    }

    @Test
    fun `bodyLargeのフォントウェイトがNormalである`() {
        assertEquals(FontWeight.Normal, Typography.bodyLarge.fontWeight)
    }

    @Test
    fun `bodyLargeのフォントサイズが16spである`() {
        assertEquals(16.sp, Typography.bodyLarge.fontSize)
    }

    @Test
    fun `bodyLargeのlineHeightが24spである`() {
        assertEquals(24.sp, Typography.bodyLarge.lineHeight)
    }

    @Test
    fun `bodyLargeのletterSpacingが0_5spである`() {
        assertEquals(0.5.sp, Typography.bodyLarge.letterSpacing)
    }
}
