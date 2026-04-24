package kurou.androidpods.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.androidpods.core.domain.OverlayPosition
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OverlayPositionRepositoryImplTest {
    private lateinit var repository: OverlayPositionRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val dataStore =
            PreferenceDataStoreFactory.create {
                File(context.filesDir, "datastore/test_overlay_position_${UUID.randomUUID()}.preferences_pb")
            }
        repository = OverlayPositionRepositoryImpl(dataStore)
    }

    @Test
    fun `デフォルト値はBOTTOM`() =
        runTest {
            val result = repository.observe().first()

            assertEquals(OverlayPosition.BOTTOM, result)
        }

    @Test
    fun `updateで保存した値が取得できる`() =
        runTest {
            repository.update(OverlayPosition.TOP)

            val result = repository.observe().first()
            assertEquals(OverlayPosition.TOP, result)
        }
}
