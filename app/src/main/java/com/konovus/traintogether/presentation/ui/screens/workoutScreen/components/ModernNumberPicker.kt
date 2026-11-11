package com.konovus.traintogether.presentation.ui.screens.workoutScreen.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay


@Composable
fun ModernNumberPicker(
    label: String,
    range: IntRange,
    selected: Int,
    onValueChange: (Int) -> Unit,
    color: Color = MaterialTheme.colorScheme.primary,
    suffix: String = ""
) {
    val itemHeight = 50.dp
    var isIncrementPressed by remember { mutableStateOf(false) }
    var isDecrementPressed by remember { mutableStateOf(false) }

    // Handle increment
    LaunchedEffect(isIncrementPressed) {
        if (isIncrementPressed) {
            // First immediate increment
            var currentValue = selected
            currentValue = (currentValue + 1).coerceIn(range)
            onValueChange(currentValue)
            delay(500L) // Initial delay before continuous

            var speed = 200L
            while (isIncrementPressed && currentValue < range.last) {
                delay(speed)
                currentValue = (currentValue + 1).coerceIn(range)
                onValueChange(currentValue)
                speed = (speed * 0.8f).toLong().coerceAtLeast(50L)
            }
        }
    }

    // Handle decrement
    LaunchedEffect(isDecrementPressed) {
        if (isDecrementPressed) {
            // First immediate decrement
            var currentValue = selected
            currentValue = (currentValue - 1).coerceIn(range)
            onValueChange(currentValue)
            delay(500L) // Initial delay before continuous

            var speed = 200L
            while (isDecrementPressed && currentValue > range.first) {
                delay(speed)
                currentValue = (currentValue - 1).coerceIn(range)
                onValueChange(currentValue)
                speed = (speed * 0.8f).toLong().coerceAtLeast(50L)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            ),
            color = color,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            modifier = Modifier
                .width(80.dp)
                .height(itemHeight * 3),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Up Arrow Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isIncrementPressed = true
                                    try {
                                        awaitRelease()
                                    } finally {
                                        isIncrementPressed = false
                                    }
                                }
                            )
                        },
                    color = Color.Transparent,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Increase",
                            tint = if (selected < range.last) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.3f
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Number Display
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${selected}$suffix",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Down Arrow Button
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isDecrementPressed = true
                                    try {
                                        awaitRelease()
                                    } finally {
                                        isDecrementPressed = false
                                    }
                                }
                            )
                        },
                    color = Color.Transparent,
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Decrease",
                            tint = if (selected > range.first) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(
                                alpha = 0.3f
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
