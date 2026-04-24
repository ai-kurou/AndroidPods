package kurou.androidpods

import android.accessibilityservice.AccessibilityService
import android.bluetooth.BluetoothManager
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kurou.androidpods.feature.onboarding.R as OnboardingR
import kurou.androidpods.feature.settings.R as SettingsR

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 2)
    val grantPermissionRule: GrantPermissionRule =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GrantPermissionRule.grant(
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN,
            )
        } else {
            GrantPermissionRule.grant(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }

    @Test
    fun `MainActivityが起動してボタンを4回押してDevicesScreenが表示される`() {
        val activity = composeTestRule.activity

        // Page 0: 「次へ」ボタンを押してページ1に遷移
        val nextText = activity.getString(OnboardingR.string.onboarding_button_next)
        composeTestRule.onNodeWithText(nextText).performClick()

        // Page 1: 「権限を許可する」ボタンを押してページ2に遷移
        // (GrantPermissionRuleで権限は自動付与済み)
        val grantPermissionText = activity.getString(OnboardingR.string.onboarding_button_grant_permission)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithText(grantPermissionText)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithText(grantPermissionText).performClick()

        // Page 2: 「オーバーレイを許可する」ボタンを押す
        // canDrawOverlays==trueなら直接次ページへ、falseなら設定画面が開くので戻るボタンで閉じる
        val allowOverlayText = activity.getString(OnboardingR.string.onboarding_button_allow_overlay)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithText(allowOverlayText)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        val canDrawOverlays = Settings.canDrawOverlays(activity)
        composeTestRule.onNodeWithText(allowOverlayText).performClick()
        if (!canDrawOverlays) {
            Thread.sleep(1_000)
            InstrumentationRegistry
                .getInstrumentation()
                .uiAutomation
                .performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }

        val bluetoothAdapter = activity.getSystemService(BluetoothManager::class.java)?.adapter
        val okText = activity.getString(android.R.string.ok)
        val enableBluetoothText = activity.getString(OnboardingR.string.onboarding_button_enable_bluetooth)
        if (bluetoothAdapter == null) {
            // Bluetoothアダプタなし（エミュレータ等）: ダイアログが表示されるのでOKを押してオンボーディング完了
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                composeTestRule.onAllNodesWithText(okText).fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText(okText).performClick()
        } else {
            // Page 2: 「BluetoothをONにする」ボタンを押してオンボーディング完了
            // BluetoothアダプタありだがBluetoothオフになっている場合はここで失敗するが許容する。
            // Bluetoothはオンでテストを実行し、InstrumentedTestでは画面遷移のみ確認できれば良いため。
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                composeTestRule
                    .onAllNodesWithText(enableBluetoothText)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            composeTestRule.onNodeWithText(enableBluetoothText).performClick()
        }

        val overlaySettingLabel = activity.getString(SettingsR.string.overlay_setting_label)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithText(overlaySettingLabel)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        val compatibleDevices = activity.getString(SettingsR.string.compatible_devices)
        composeTestRule.onNodeWithText(compatibleDevices).performScrollTo().performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithText(compatibleDevices)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        val openSourceLicenses = activity.getString(SettingsR.string.open_source_licenses)
        composeTestRule.onNodeWithText(openSourceLicenses).performScrollTo().performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithText(openSourceLicenses)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
    }
}
