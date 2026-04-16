package kurou.androidpods

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-port-xxhdpi")
class PreviewScreenshotTest(
    private val preview: ComposablePreview<AndroidPreviewInfo>,
) {
    @get:Rule
    val composeTestRule = createComposeRule()

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun previews(): List<ComposablePreview<AndroidPreviewInfo>> =
            AndroidComposablePreviewScanner()
                .scanPackageTrees("kurou.androidpods")
                .includePrivatePreviews()
                .getPreviews()
    }

    @Test
    fun snapshot() {
        composeTestRule.setContent {
            preview.invoke()
        }
        // name だけ残し（同一関数に複数 @Preview がある場合の区別に必要）、
        // widthDp・showBackground などの冗長なパラメータは除外する
        val idParts = AndroidPreviewScreenshotIdBuilder(preview)
            .ignoreIdFor("group")
            .ignoreIdFor("apiLevel")
            .ignoreIdFor("widthDp")
            .ignoreIdFor("heightDp")
            .ignoreIdFor("locale")
            .ignoreIdFor("fontScale")
            .ignoreIdFor("showSystemUi")
            .ignoreIdFor("showBackground")
            .ignoreIdFor("backgroundColor")
            .ignoreIdFor("uiMode")
            .ignoreIdFor("device")
            .build()
            .split('.')
        // name が空のとき → クラス名+関数名 (末尾2要素)
        // name があるとき → クラス名+関数名+name (末尾3要素)
        val takeCount = if (preview.previewInfo.name.isEmpty()) 2 else 3
        val screenshotId = idParts.takeLast(takeCount).joinToString(".")
        composeTestRule.onRoot().captureRoboImage(
            filePath = "$screenshotId.png",
        )
    }
}
