package kurou.androidpods.core.data

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class FirstLaunchRepositoryImplTest {
    private val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    private val repository = FirstLaunchRepositoryImpl(context)

    @Test
    fun `初回起動時はtrueを返す`() =
        runTest {
            val isFirstLaunch = repository.observeIsFirstLaunch().first()

            assertTrue(isFirstLaunch)
        }

    @Test
    fun `markAsLaunched後はfalseを返す`() =
        runTest {
            repository.markAsLaunched()

            val isFirstLaunch = repository.observeIsFirstLaunch().first()
            assertFalse(isFirstLaunch)
        }
}
