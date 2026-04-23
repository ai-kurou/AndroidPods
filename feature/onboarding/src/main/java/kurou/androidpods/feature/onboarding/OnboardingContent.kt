package kurou.androidpods.feature.onboarding

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

internal const val PAGE_COUNT = 4
internal const val PERMISSION_PAGE = 1
internal const val OVERLAY_PAGE = 2
internal const val BLUETOOTH_PAGE = 3

internal data class OnboardingPageData(
    @RawRes val lottieResId: Int,
    @StringRes val textResId: Int,
)

internal val pages =
    listOf(
        OnboardingPageData(
            lottieResId = R.raw.charging,
            textResId = R.string.onboarding_page1_text,
        ),
        OnboardingPageData(
            lottieResId = R.raw.location,
            textResId = R.string.onboarding_page2_text,
        ),
        OnboardingPageData(
            lottieResId = R.raw.overlay,
            textResId = R.string.onboarding_page3_text,
        ),
        OnboardingPageData(
            lottieResId = R.raw.bluetooth,
            textResId = R.string.onboarding_page4_text,
        ),
    )

@Composable
internal fun OnboardingContent(
    pagerState: PagerState,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->
            OnboardingPage(pageData = pages[page])
        }

        PageIndicator(
            pageCount = PAGE_COUNT,
            currentPage = pagerState.currentPage,
            modifier = Modifier.padding(16.dp),
        )

        Button(
            onClick = onButtonClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
        ) {
            Text(
                text =
                    stringResource(
                        when {
                            pagerState.currentPage == PERMISSION_PAGE -> R.string.onboarding_button_grant_permission
                            pagerState.currentPage == OVERLAY_PAGE -> R.string.onboarding_button_allow_overlay
                            pagerState.currentPage == BLUETOOTH_PAGE -> R.string.onboarding_button_enable_bluetooth
                            else -> R.string.onboarding_button_next
                        },
                    ),
            )
        }
    }
}

@Composable
private fun OnboardingPage(
    pageData: OnboardingPageData,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.screenHeightDp > configuration.screenWidthDp

    if (isPortrait) {
        Column(
            modifier = modifier.fillMaxSize().padding(36.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LottieContent(
                lottieResId = pageData.lottieResId,
                modifier = Modifier.size(200.dp),
            )
            Spacer(modifier = Modifier.height(64.dp))
            Text(
                text = stringResource(pageData.textResId),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
        }
    } else {
        Row(
            modifier = modifier.fillMaxSize().padding(36.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LottieContent(
                lottieResId = pageData.lottieResId,
                modifier = Modifier.size(200.dp),
            )
            Spacer(modifier = Modifier.width(64.dp))
            Text(
                text = stringResource(pageData.textResId),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun LottieContent(
    @RawRes lottieResId: Int,
    modifier: Modifier = Modifier,
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier.semantics { testTag = "lottie_animation" },
    )
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                        ),
            )
        }
    }
}

@Preview(showBackground = true, name = "Portrait", widthDp = 400, heightDp = 700)
@Preview(showBackground = true, name = "Landscape", widthDp = 700, heightDp = 400)
@Composable
private fun OnboardingContentPreview() {
    OnboardingContent(
        pagerState = rememberPagerState(pageCount = { PAGE_COUNT }),
        onButtonClick = {},
    )
}
