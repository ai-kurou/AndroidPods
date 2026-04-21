package kurou.androidpods

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.android.screenshotid.AndroidPreviewScreenshotIdBuilder
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
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
        // @Previewの画面サイズを使う
        // サイズが指定されていない場合はRobolectricのデフォルトの小さい画面になる
        val w = preview.previewInfo.widthDp
        val h = preview.previewInfo.heightDp
        if (w > 0 && h > 0) {
            val orientation = if (w > h) "land" else "port"
            RuntimeEnvironment.setQualifiers("w${w}dp-h${h}dp-$orientation-xxhdpi")
        }
        composeTestRule.setContent {
            preview.invoke()
        }
        // name だけ残し（同一関数に複数 @Preview がある場合の区別に必要）、
        // widthDp・showBackground などの冗長なパラメータは除外する
        val idParts = AndroidPreviewScreenshotIdBuilder(preview)
            .ignoreIdFor("widthDp")
            .ignoreIdFor("heightDp")
            .ignoreIdFor("showBackground")
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
