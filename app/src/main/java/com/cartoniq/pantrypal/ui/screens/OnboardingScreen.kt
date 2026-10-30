package com.cartoniq.pantrypal.ui.screens

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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartoniq.pantrypal.R
import kotlinx.coroutines.launch

private data class OnboardingPage(val emoji: Int, val title: Int, val body: Int)

@Composable
fun OnboardingScreen(onFinish: (loadSample: Boolean) -> Unit) {
    val pages = listOf(
        OnboardingPage(R.string.onb_emoji_1, R.string.onb_title_1, R.string.onb_body_1),
        OnboardingPage(R.string.onb_emoji_2, R.string.onb_title_2, R.string.onb_body_2),
        OnboardingPage(R.string.onb_emoji_3, R.string.onb_title_3, R.string.onb_body_3)
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (!isLast) {
                    TextButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pages.lastIndex) } }
                    ) {
                        Text(stringResource(R.string.onb_skip))
                    }
                } else {
                    Spacer(Modifier.height(48.dp))
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
                val page = pages[index]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = stringResource(page.emoji), fontSize = 72.sp)
                    }
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = stringResource(page.title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(page.body),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pages.indices.forEach { i ->
                    Box(
                        Modifier
                            .size(if (i == pagerState.currentPage) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (i == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }
                            )
                    )
                }
            }

            if (isLast) {
                Button(
                    onClick = { onFinish(true) },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.onb_load_sample))
                }
                OutlinedButton(
                    onClick = { onFinish(false) },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.onb_start_empty))
                }
            } else {
                Button(
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.onb_next))
                }
            }
        }
    }
}
