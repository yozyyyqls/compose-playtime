package com.yozyyy.composeplaytime

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.yozyyy.composeplaytime.ui.theme.ComposePlaytimeTheme
import kotlin.math.absoluteValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposePlaytimeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainPage(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

fun PagerState.calculateCurrentOffsetForPage(page: Int): Float {
    Log.d(
        "Movie",
        "currentPage=$currentPage, page=$page, currentPageOffsetFraction=$currentPageOffsetFraction,calculateCurrentOffsetForPage: ${(currentPage - page) + currentPageOffsetFraction}"
    )
    return (currentPage - page) + currentPageOffsetFraction
}

const val animationDuration = 2000

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MainPage(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { movieData.size })
    Crossfade(
        targetState = pagerState.currentPage,
        animationSpec = tween(500),
        label = "background image cross fade"
    ) { currentPage ->
        val pageOffset = pagerState.currentPageOffsetFraction
        Image(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = lerp(1f, 1.1f, pageOffset.absoluteValue)
                    scaleY = lerp(1f, 1.1f, pageOffset.absoluteValue)
                    translationY = lerp(0f, -20f, pageOffset.absoluteValue)
                }
                .drawWithCache {
                    val gradient = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.White),
                        startY = 0f,
                        endY = size.height / 1.5f
                    )
                    onDrawWithContent {
                        drawContent()
                        drawRect(gradient, blendMode = BlendMode.Lighten)
                    }
                },
            painter = painterResource(movieData[currentPage].resId),
            contentDescription = "",
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.TopCenter
        )
    }

    var isExpanded by remember { mutableStateOf(false) }
    SharedTransitionLayout {
        AnimatedContent(
            targetState = isExpanded,
            label = "basic transition"
        ) { targetState ->
            if (!targetState) {
                HorizontalPager(
                    state = pagerState,
                    modifier = modifier.fillMaxSize(),
                    verticalAlignment = Alignment.Bottom,
                    pageSpacing = 20.dp,
                    contentPadding = PaddingValues(horizontal = 50.dp)
                ) { page ->
                    val pageOffset = pagerState.calculateCurrentOffsetForPage(page)
                    val cardCornerAnimation by
                    (this@AnimatedContent as AnimatedVisibilityScope).transition.animateDp(
                        label = "movie card corner animation",
                        transitionSpec = {
                            tween(animationDuration)
                        }) { state: EnterExitState ->
                        when (state) {
                            EnterExitState.PreEnter -> 0.dp
                            EnterExitState.Visible -> 130.dp
                            EnterExitState.PostExit -> 0.dp
                        }
                    }
                    MovieCard(
                        modifier = Modifier
                            .padding(bottom = lerp(96.dp, 56.dp, pageOffset.absoluteValue))
                            .width(260.dp)
                            .height(480.dp)
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "movie$page"),
                                animatedVisibilityScope = this@AnimatedContent,
                                boundsTransform = { _, _ -> tween(animationDuration) },
                                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(cardCornerAnimation))
                            )
                            .graphicsLayer {
                                clip = true
                                shape = RoundedCornerShape(cardCornerAnimation)
                                shadowElevation = 30f
                                spotShadowColor = DefaultShadowColor.copy(alpha = 0.5f)
                                ambientShadowColor = DefaultShadowColor.copy(alpha = 0.5f)
                                scaleY = lerp(1f, 0.9f, pageOffset.absoluteValue)
                            }
                            .background(color = Color.White)
                            .padding(top = 32.dp, start = 32.dp, end = 32.dp),
                        page = page,
                        movie = movieData[page],
                        onClickImage = {
                            isExpanded = !isExpanded
                        },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent
                    )
                }
            } else {
                MovieDetail(
                    page = pagerState.currentPage,
                    movieData[pagerState.currentPage],
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent,
                ) {
                    isExpanded = !isExpanded
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MovieCard(
    modifier: Modifier,
    page: Int,
    movie: MovieItem,
    onClickImage: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    val posterCornerAnimation by animatedVisibilityScope.transition.animateDp(
        label = "movie poster corner animation",
        transitionSpec = {
            tween(animationDuration)
        }) { state: EnterExitState ->
        when (state) {
            EnterExitState.PreEnter -> 0.dp
            EnterExitState.Visible -> 100.dp
            EnterExitState.PostExit -> 0.dp
        }
    }
    with(sharedTransitionScope) {
        Box(modifier = modifier) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(movie.resId),
                    contentDescription = "",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            onClickImage()
                        }
                        .sharedElement(
                            state = rememberSharedContentState(key = "image$page"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                tween(durationMillis = animationDuration)
                            },
                        )
                        .fillMaxWidth()
                        .height(290.dp)
                        .clip(RoundedCornerShape(posterCornerAnimation))
                )
                Text(
                    text = movie.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "movie_name$page"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ -> tween(animationDuration) }
                        )
                )
            }
            BookNow(
                modifier = Modifier
                    .sharedElement(
                        state = rememberSharedContentState(key = "booknow$page"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = {_, _ -> tween(animationDuration)}
                    )
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.Black)
            )
        }
    }
}

@Composable
fun BookNow(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "BOOK NOW",
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MovieDetail(
    page: Int,
    movie: MovieItem,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClickImage: () -> Unit,
) {
    val detailPageCornerAnimation by
    animatedVisibilityScope.transition.animateDp(
        label = "movie detail corner animation",
        transitionSpec = {
            tween(animationDuration)
        }) { state: EnterExitState ->
        when (state) {
            EnterExitState.PreEnter -> 130.dp
            EnterExitState.Visible -> 0.dp
            EnterExitState.PostExit -> 130.dp
        }
    }
    val posterCornerAnimation by animatedVisibilityScope.transition.animateDp(
        label = "movie poster corner animation",
        transitionSpec = {
            tween(animationDuration)
        }) { state: EnterExitState ->
        when (state) {
            EnterExitState.PreEnter -> 100.dp
            EnterExitState.Visible -> 0.dp
            EnterExitState.PostExit -> 100.dp
        }
    }
    with(sharedTransitionScope) {
        Box(
            modifier = Modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "movie$page"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ -> tween(animationDuration) },
                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                    clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(detailPageCornerAnimation))
                )
                .fillMaxSize()
                .clip(RoundedCornerShape(detailPageCornerAnimation))
                .background(Color.White)
        ) {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Image( // 电影海报
                    painter = painterResource(movie.resId),
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            onClickImage()
                        }
                        .sharedElement(
                            state = rememberSharedContentState(key = "image$page"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                tween(durationMillis = animationDuration)
                            },
                        )
                        .fillMaxWidth()
                        .height(520.dp)
                        .clip(RoundedCornerShape(posterCornerAnimation)),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = null
                )
                Text( // 电影名称
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 20.dp)
                        .align(Alignment.CenterHorizontally)
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "movie_name$page"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ -> tween(animationDuration) }
                        ),
                    text = movie.name,
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                )
                // 电影详细内容（演员列表，电影故事线）
                DetailContent(movie.description)
            }

            val context = LocalContext.current
            BookNow( // 购买按钮
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        Toast
                            .makeText(context, "Book Now", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .padding(bottom = 16.dp)
                    .sharedElement(
                        state = rememberSharedContentState(key = "booknow$page"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = {_, _ -> tween(animationDuration)},
                    )
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.Black)
            )
        }
    }
}

/**
 * 电影详细内容（演员列表，电影故事线）
 */
@Composable
fun DetailContent(movieDescription: String) {
    Column(modifier = Modifier.padding(top = 16.dp, bottom = 100.dp)) {
        DetailSubTitle("Cast")
        Cast()
        DetailSubTitle("Storyline")
        Text(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .wrapContentHeight(), text = movieDescription,
            color = Color.Gray
        )
    }
}

/**
 * 电影详细内容标题
 */
@Composable
fun DetailSubTitle(text: String) {
    Text(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        text = text,
        style = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.DarkGray
        )
    )
}

/**
 * 演员列表
 */
@Composable
fun Cast() {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .height(100.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .padding(end = 24.dp)
                .fillMaxHeight()
                .width(100.dp)
                .graphicsLayer {
                    clip = true
                    shape = RoundedCornerShape(10.dp)
                }
                .background(Color.LightGray)
        )
        Box(
            modifier = Modifier
                .padding(end = 24.dp)
                .fillMaxHeight()
                .width(100.dp)
                .graphicsLayer {
                    clip = true
                    shape = RoundedCornerShape(10.dp)
                }
                .background(Color.LightGray)
        )
        Box(
            modifier = Modifier
                .padding(end = 24.dp)
                .fillMaxHeight()
                .width(100.dp)
                .graphicsLayer {
                    clip = true
                    shape = RoundedCornerShape(10.dp)
                }
                .background(Color.LightGray)
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=950px,height=2000px,orientation=portrait,dpi=429"
)
@Composable
fun MainPagePreview() {
    ComposePlaytimeTheme {
        MainPage()
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=950px,height=2000px,orientation=portrait,dpi=429"
)
@Composable
fun MovieDetailPreview() {
    ComposePlaytimeTheme {
//        MovieDetail(movieData[0]) {}
    }
}