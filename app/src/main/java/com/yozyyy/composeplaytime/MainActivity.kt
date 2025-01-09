package com.yozyyy.composeplaytime

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
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
    Log.d("Movie", "currentPage=$currentPage, page=$page, currentPageOffsetFraction=$currentPageOffsetFraction,calculateCurrentOffsetForPage: ${(currentPage - page) + currentPageOffsetFraction}")
    return (currentPage - page) + currentPageOffsetFraction
}

@Composable
fun MainPage(modifier: Modifier = Modifier) {
    Image(
        modifier = Modifier
            .fillMaxSize()
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
        painter = painterResource(R.drawable.robot_dreams),
        contentDescription = "",
        contentScale = ContentScale.FillWidth,
        alignment = Alignment.TopCenter
    )

    val pagerState = rememberPagerState(pageCount = { movieData.size })
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.Bottom,
        pageSpacing = 20.dp,
        contentPadding = PaddingValues(horizontal = 50.dp)
    ) { page ->
        val pageOffset = pagerState.calculateCurrentOffsetForPage(page)
        MovieCard(
            modifier = Modifier
                .padding(bottom = lerp(96.dp, 56.dp, pageOffset.absoluteValue))
                .width(260.dp)
                .height(480.dp)
                .graphicsLayer {
                    clip = true
                    shape = RoundedCornerShape(130.dp)
                    shadowElevation = 30f
                    spotShadowColor = DefaultShadowColor.copy(alpha = 0.5f)
                    ambientShadowColor = DefaultShadowColor.copy(alpha = 0.5f)
                    scaleY = lerp(1f, 0.9f, pageOffset.absoluteValue)
                }
                .background(color = Color.White)
                .padding(top = 32.dp, start = 32.dp, end = 32.dp),
            page = page,
            movie = movieData[page]
        )
    }
}

@Composable
fun MovieCard(modifier: Modifier, page: Int, movie: MovieItem) {
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
                    .fillMaxWidth()
                    .height(290.dp)
                    .clip(RoundedCornerShape(100.dp))
            )
            Text(
                text = movie.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
        BookNow(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color.Black)
        )
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

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=950px,height=2000px,orientation=portrait,dpi=429"
)
@Composable
fun GreetingPreview() {
    ComposePlaytimeTheme {
        MainPage()
    }
}