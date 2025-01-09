package com.yozyyy.composeplaytime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.yozyyy.composeplaytime.ui.theme.ComposePlaytimeTheme

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
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposePlaytimeTheme {
        MainPage()
    }
}