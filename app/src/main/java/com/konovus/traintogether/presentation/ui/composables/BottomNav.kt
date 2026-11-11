package com.konovus.traintogether.presentation.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.konovus.traintogether.navigation.Actions
import com.konovus.traintogether.navigation.Screens

@Composable
fun BottomBar(
    navController: NavHostController,
    actions: Actions,
) {
    val navStackBackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navStackBackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets(0, 0, 0, 0))
    ) {
        // Main bottom bar with cutout
        val surfaceColor = MaterialTheme.colorScheme.surface

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(MaterialTheme.colorScheme.surface)
                .navigationBarsPadding()
        ) {
            val path = Path().apply {
                // Start from top left
                moveTo(0f, 0f)

                // Draw to the start of the cutout
                val cutoutCenter = size.width / 2f
                val cutoutRadius = 35.dp.toPx()
                val cutoutStart = cutoutCenter - cutoutRadius - 10.dp.toPx()

                lineTo(cutoutStart, 0f)

                // Create the semicircle cutout
                arcTo(
                    rect = Rect(
                        left = cutoutCenter - cutoutRadius - 10.dp.toPx(),
                        top = -cutoutRadius,
                        right = cutoutCenter + cutoutRadius + 10.dp.toPx(),
                        bottom = cutoutRadius
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )

                // Continue to top right
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }

            // Draw shadow first
            drawPath(
                path = path,
                color = Color.Black.copy(alpha = 0.1f),
                style = Stroke(width = 1.dp.toPx())
            )

            // Draw main background
            drawPath(
                path = path,
                color = surfaceColor
            )
        }

        // Navigation items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side items (Home, Stats)
            // Use your existing Screens logic
            Screens.all.filter { it.icon != null }.forEachIndexed { index, screen ->

                if (index == 2)
                    Spacer(modifier = Modifier.width(40.dp))

                AddItem(
                    screen = screen,
                    currentDestination = currentDestination,
                    actions = actions
                )
            }
        }

        // Floating Add Button
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-25).dp),
            contentAlignment = Alignment.Center
        ) {
            // Button shadow/glow effect
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        CircleShape
                    )
                    .blur(8.dp)
            )

            // Main button
            Surface(
                onClick = {
                    actions.navigateTo(Screens.AddWorkout.route, params = listOf(0))
                },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .size(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Workout",
                        tint = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddItem(
    screen: Screens,
    currentDestination: NavDestination?,
    actions: Actions,
) {
    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                actions.navigateTo(screen.route, emptyList())
            }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(if (selected) 32.dp else 28.dp)
                .background(
                    if (selected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else
                        Color.Transparent,
                    RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                imageVector = screen.icon!!,
                contentDescription = screen.title,
                tint = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = screen.title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                letterSpacing = 0.5.sp
            ),
            color = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.secondary,
            maxLines = 1
        )
    }
}
