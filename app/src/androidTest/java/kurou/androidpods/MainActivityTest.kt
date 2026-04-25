package kurou.androidpods

import android.accessibilityservice.AccessibilityService
import android.bluetooth.BluetoothManager
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
    fun `MainActivity起動後_オンボーディングを経てSettingsからDevicesとLicensesへ遷移できる`() {
        // オンボーディング: Page 0 「Next」
        composeTestRule.onNodeWithText("Next").performClick()

        // オンボーディング: Page 1 「Grant Permission」（GrantPermissionRule で自動付与済み）
        waitForText("Grant Permission")
        composeTestRule.onNodeWithText("Grant Permission").performClick()

        // オンボーディング: Page 2 「Allow Overlay」
        // canDrawOverlays==false の場合は設定画面が開くので戻るボタンで閉じる
        waitForText("Allow Overlay")
        composeTestRule.onNodeWithText("Allow Overlay").performClick()
        if (!Settings.canDrawOverlays(composeTestRule.activity)) {
            Thread.sleep(1_000)
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        }

        // オンボーディング: Page 3 Bluetooth
        val bluetoothAdapter = composeTestRule.activity
            .getSystemService(BluetoothManager::class.java)?.adapter
        if (bluetoothAdapter == null) {
            // エミュレータ等 Bluetooth なし: OK ダイアログを閉じてオンボーディング完了
            waitForText("OK")
            composeTestRule.onNodeWithText("OK").performClick()
        } else {
            // Bluetooth オフの実機では失敗するが許容（画面遷移の確認が目的）
            waitForText("Enable Bluetooth")
            composeTestRule.onNodeWithText("Enable Bluetooth").performClick()
        }

        // SettingsScreen が表示されることを確認
        waitForText("Show battery overlay")

        // DevicesScreen に遷移して戻る
        composeTestRule.onNodeWithTag("SettingsGrid").performScrollToNode(hasText("Compatible devices"))
        composeTestRule.onNodeWithText("Compatible devices").performClick()
        waitForTag("DevicesScreen")
        navigateBack()
        waitForText("Show battery overlay")

        // LicensesScreen に遷移して戻る
        composeTestRule.onNodeWithTag("SettingsGrid").performScrollToNode(hasText("Open Source Licenses"))
        composeTestRule.onNodeWithText("Open Source Licenses").performClick()
        waitForTag("LicensesScreen")
        navigateBack()
    }

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForTag(tag: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun navigateBack() {
        composeTestRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }
    }
}
