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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.activity.compose.BackHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 3

private data class OnboardingPageData(
    @RawRes val lottieResId: Int,
    @StringRes val textResId: Int,
)

private val pages = listOf(
    OnboardingPageData(
        lottieResId = R.raw.charging,
        textResId = R.string.onboarding_page1_text,
    ),
    OnboardingPageData(
        lottieResId = R.raw.onboarding_page2,
        textResId = R.string.onboarding_page2_text,
    ),
    OnboardingPageData(
        lottieResId = R.raw.onboarding_page3,
        textResId = R.string.onboarding_page3_text,
    ),
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = pagerState.currentPage > 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
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
            onClick = {
                if (pagerState.currentPage < PAGE_COUNT - 1) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    onComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            Text(
                text = stringResource(
                    if (pagerState.currentPage < PAGE_COUNT - 1) R.string.onboarding_button_next
                    else R.string.onboarding_button_get_started,
                ),
            )
        }
    }
}

@Composable
private fun OnboardingPage(pageData: OnboardingPageData, modifier: Modifier = Modifier) {
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
                textAlign = TextAlign.Center
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
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LottieContent(@RawRes lottieResId: Int, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier,
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
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                    ),
            )
        }
    }
}

@Preview(showBackground = true, name = "Portrait", device = "spec:width=411dp,height=891dp")
@Preview(showBackground = true, name = "Landscape", device = "spec:width=891dp,height=411dp")
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(onComplete = {})
}
