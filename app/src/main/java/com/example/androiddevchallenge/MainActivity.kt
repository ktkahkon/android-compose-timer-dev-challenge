/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.min
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp()
            }
        }
    }
}

@Composable
fun MyApp() {
    var currentPage by remember { mutableStateOf("Intro") }

    Crossfade(targetState = currentPage) { screen ->
        when (screen) {
            "Intro" -> IntroScreen(launch = { currentPage = "CountDown" })
            "CountDown" -> CountDown()
        }
    }
}

@Composable
fun IntroScreen(
    launch: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        val showTitle = remember { mutableStateOf(false) }
        val showLogo = remember { mutableStateOf(false) }
        var loading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            delay(700)
            showTitle.value = true
            delay(500)
            showLogo.value = true
            delay(2500)
            loading = false
        }

        val alpha: Float by animateFloatAsState(
            targetValue = if (showLogo.value) 1f else 0f,
            animationSpec = keyframes {
                durationMillis = 800
                0.0f at 0 with LinearEasing
            }
        )

        Column(
            Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(top = 40.dp)
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                if (showTitle.value) {
                    AnimatedText(
                        text = "CORDS",
                        style = MaterialTheme.typography.h2.copy(fontWeight = FontWeight.ExtraBold)
                    )

                    Image(
                        painter = painterResource(id = R.drawable.compose_icon),
                        contentDescription = null,
                        alpha = alpha,
                        modifier = Modifier.requiredSize(130.dp, 130.dp)
                    )
                }
            }

            AnimatedText(text = "COMPOSE RELEASE AND DISTRIBUTION SYSTEM")

            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (loading) {
                    CustomProgressIndicator()
                } else {
                    OutlinedButton(
                        onClick = launch,
                        shape = AbsoluteCutCornerShape(topLeft = 10.dp, bottomRight = 10.dp)
                    ) {
                        Text("LAUNCH A NEW VERSION")
                    }
                }
            }
        }
    }
}

@Composable
fun CountDown() {
    var number by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        (0..6).forEach {
            delay(1000)
            number = 6 - it
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        val count = number
        if (count != null && count > 0) {
            Box(contentAlignment = Alignment.Center) {
                CountDownNumber(number = count.toString())
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth()
            ) {
                AnimatedText(text = "LAUNCHING IN...")
            }
        } else if (count == 0) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth()
            ) {
                AnimatedText(text = "LAUNCH COMPLETED")
            }
        }

        AnimatedGrid()
    }
}

class GridAnimation {
    private var prevNanos: Long = Long.MAX_VALUE
    var positions by mutableStateOf<List<Pair<Int, Float>>>(emptyList())
    val random = Random(System.currentTimeMillis())

    fun update(nanos: Long) {
        val dt = (nanos - prevNanos).coerceAtLeast(0)
        prevNanos = nanos
        val speed = (dt / 1E9 * 50).toFloat()

        val updatedPositions = positions
            .map { Pair(it.first, it.second - speed) }
            .filter { it.second > -10 }

        val newPositions = if (positions.size < 15 && random.nextInt(100) <= 6) {
            listOf(Pair(random.nextInt(25), 100f))
        } else {
            emptyList()
        }

        positions = updatedPositions + newPositions
    }
}

@Composable
fun AnimatedGrid() {
    val color = Color(0x22302041)

    val backgroundAnimation = remember { GridAnimation() }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                backgroundAnimation.update(it)
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 15.dp.toPx()
        val numberOfHorizontalLines = (size.width / gridSize).toInt()
        val numberOfVerticalLines = (size.height / gridSize).toInt()

        (1..numberOfVerticalLines).forEach {
            drawLine(
                color = color,
                start = Offset(0f, it * gridSize),
                end = Offset(size.width, it * gridSize),
                strokeWidth = 1.dp.toPx()
            )
        }

        (1..numberOfHorizontalLines).forEach {
            drawLine(
                color = color,
                start = Offset(it * gridSize, 0f),
                end = Offset(it * gridSize, size.height),
                strokeWidth = 1.dp.toPx()
            )
        }

        backgroundAnimation.positions.forEach {
            drawLine(
                brush = Brush.linearGradient(
                    0.0f to Color.White,
                    0.3f to Color.White.copy(alpha = 0.9f),
                    1.0f to Color.White.copy(alpha = 0.0f),
                    start = Offset(it.first * gridSize, size.height * min(it.second / 100, 1f)),
                    end = Offset(
                        it.first * gridSize, size.height * min((it.second + 7) / 100, 1f)
                    )
                ),
                start = Offset(it.first * gridSize, size.height * min(it.second / 100, 1f)),
                end = Offset(
                    it.first * gridSize, size.height * min((it.second + 7) / 100, 1f)
                )
            )
        }
    }
}

@Composable
fun CountDownNumber(number: String) {
    val progress = remember(number) { Animatable(0f) }

    LaunchedEffect(number) {
        progress.animateTo(
            100f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutLinearInEasing)
        )
    }

    Text(
        number,
        style = MaterialTheme.typography.h1.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 195.sp
        ),
        color = Color.White.copy(alpha = progress.value / 100f)
    )
}

@Composable
fun AnimatedText(text: String, style: TextStyle = LocalTextStyle.current) {
    val progress1 = remember { Animatable(0f) }
    val progress2 = remember { Animatable(-20f) }

    LaunchedEffect(Unit) {
        progress1.animateTo(
            100f,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
        )
    }

    LaunchedEffect(Unit) {
        progress2.animateTo(
            100f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )
    }

    val characters1 = (text.length * (progress1.value / 100)).toInt()
    val characters2 = if (progress2.value >= 0) {
        (text.length * (progress2.value / 100)).toInt()
    } else {
        0
    }

    Box {
        Text(
            text = text.take(characters1),
            color = Color.White.copy(alpha = 0.2f),
            style = style
        )

        if (progress2.value > 0) {
            Text(
                text = text.take(characters2),
                color = Color.White.copy(alpha = 0.85f),
                style = style
            )
        }
    }
}

@Composable
fun CustomProgressIndicator() {
    var step by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(400)
            step = (step + 1) % 5
        }
    }

    Canvas(modifier = Modifier.size(250.dp, 30.dp)) {
        drawLine(
            color = Color.White.copy(alpha = if (step == 4) 0.8f else 0.2f),
            start = Offset(10f, 0f),
            end = Offset(10f, size.height),
            strokeWidth = 2.dp.toPx()
        )

        drawLine(
            color = Color.White.copy(alpha = if (step == 0) 0.8f else 0.2f),
            start = center.copy(y = 0f),
            end = center.copy(y = size.height),
            strokeWidth = 2.dp.toPx()
        )

        drawLine(
            color = Color.White.copy(alpha = if (step == 4) 0.8f else 0.2f),
            start = Offset(size.width - 10, 0f),
            end = Offset(size.width - 10, size.height),
            strokeWidth = 2.dp.toPx()
        )

        val distance = (center.x - 10) / 4
        val yOffset = ((size.height / 2) - size.height * 0.75 / 2).toFloat()

        (1..3).forEach {
            drawLine(
                color = Color.White.copy(alpha = if (step == it) 0.8f else 0.2f),
                start = Offset(x = center.x - it * distance, y = yOffset),
                end = Offset(x = center.x - it * distance, y = size.height - yOffset),
                strokeWidth = 1.dp.toPx()
            )
        }

        (1..3).forEach {
            drawLine(
                color = Color.White.copy(alpha = if (step == it) 0.8f else 0.2f),
                start = Offset(x = center.x + it * distance, y = yOffset),
                end = Offset(x = center.x + it * distance, y = size.height - yOffset),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}
