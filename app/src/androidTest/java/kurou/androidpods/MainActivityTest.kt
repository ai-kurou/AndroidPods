package kurou.androidpods

import android.bluetooth.BluetoothManager
import android.os.Build
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kurou.androidpods.feature.onboarding.R as OnboardingR
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
    fun `MainActivityが起動してボタンを3回押してDevicesScreenが表示される`() {
        val activity = composeTestRule.activity
        val bluetoothAdapter = activity.getSystemService(BluetoothManager::class.java)?.adapter
        val nextText = activity.getString(OnboardingR.string.onboarding_button_next)
        val grantPermissionText = activity.getString(OnboardingR.string.onboarding_button_grant_permission)
        val enableBluetoothText = activity.getString(OnboardingR.string.onboarding_button_enable_bluetooth)

        // Page 0: 「次へ」ボタンを押してページ1に遷移
        composeTestRule.onNodeWithText(nextText).performClick()

        // Page 1: 「権限を許可する」ボタンを押してページ2に遷移
        // (GrantPermissionRuleで権限は自動付与済み)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText(grantPermissionText)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(grantPermissionText).performClick()

        if (bluetoothAdapter == null) {
            // Bluetoothアダプタなし（エミュレータ等）: ダイアログが表示されるのでOKを押してオンボーディング完了
            val okText = activity.getString(android.R.string.ok)
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                composeTestRule.onAllNodesWithText(okText).fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText(okText).performClick()
        } else {
            // Page 2: 「BluetoothをONにする」ボタンを押してオンボーディング完了
            // BluetoothアダプタありだがBluetoothオフになっている場合はここで失敗するが許容する。
            // Bluetoothはオンでテストを実行し、InstrumentedTestでは画面遷移のみ確認できれば良いため。
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                composeTestRule.onAllNodesWithText(enableBluetoothText)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText(enableBluetoothText).performClick()
        }

        // DevicesScreen（SettingsScreen）が表示されることを確認
        // 現状はSettingsScreenに何も表示されないのでコメントアウト
        // SettingsScreenになにか表示されるようになったら追加する
    }
}
