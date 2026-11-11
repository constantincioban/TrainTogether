package com.konovus.traintogether.presentation.ui.screens.homeScreen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konovus.traintogether.data.utils.PeriodType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class StreakPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val count: Int,
    val isCurrent: Boolean = false
)


@Composable
fun StreakDetailDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    currentStreak: Int,
    streakPeriodType: PeriodType,
    checkedDays: List<LocalDate>,
    longestStreak: Int = 0,
    previousStreaks: List<StreakPeriod> = emptyList()
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {},
            text = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Header with fire icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Current Streak
                        Text(
                            text = "Current Streak",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = buildString {
                                append(currentStreak)
                                append(" ")
                                append(if (streakPeriodType == PeriodType.WEEK) {
                                    if (currentStreak == 1) "Week" else "Weeks"
                                } else {
                                    if (currentStreak == 1) "Day" else "Days"
                                })
                            },
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 48.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Progress toward next milestone
                        StreakMilestoneProgress(currentStreak, streakPeriodType)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Stats Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StreakStatCard(
                                value = longestStreak.toString(),
                                label = "Longest",
                                modifier = Modifier.weight(1f)
                            )
                            StreakStatCard(
                                value = checkedDays.size.toString(),
                                label = "Total Workouts",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (previousStreaks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(24.dp))

                            HorizontalDivider(
                                color = Color.Gray.copy(alpha = 0.2f),
                                thickness = 1.dp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Recent Streaks",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Previous streaks list
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(previousStreaks.take(5)) { streak ->
                                    PreviousStreakCard(streak, streakPeriodType)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Motivational message
                        Text(
                            text = getMotivationalMessage(currentStreak, streakPeriodType),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 20.sp
                        )
                    }

                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun StreakMilestoneProgress(
    currentStreak: Int,
    periodType: PeriodType
) {
    val milestones = if (periodType == PeriodType.WEEK) {
        listOf(4, 8, 12, 16, 26, 39, 52, 78, 104)
    } else {
        listOf(7, 14, 21, 30, 60, 90, 100, 180, 200, 365, 500, 730)
    }

    val nextMilestone = milestones.firstOrNull { it > currentStreak } ?: milestones.last()
    val previousMilestone = milestones.lastOrNull { it <= currentStreak } ?: 0
    val progress = if (nextMilestone > previousMilestone) {
        (currentStreak - previousMilestone).toFloat() / (nextMilestone - previousMilestone)
    } else {
        1f
    }

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(1000)
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Next Milestone",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray.copy(alpha = 0.8f)
            )
            Text(
                text = "$nextMilestone ${if (periodType == PeriodType.WEEK) "weeks" else "days"}",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFFFF6B35)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = animatedProgress.value,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFFFF6B35),
            trackColor = Color(0xFF2A2A2E)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${nextMilestone - currentStreak} more to go!",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun StreakStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PreviousStreakCard(
    streak: StreakPeriod,
    periodType: PeriodType
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .width(100.dp)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFF6B35).copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = streak.count.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (periodType == PeriodType.WEEK) {
                    if (streak.count == 1) "week" else "weeks"
                } else {
                    if (streak.count == 1) "day" else "days"
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${streak.startDate.format(DateTimeFormatter.ofPattern("MMM d"))} - ${streak.endDate.format(DateTimeFormatter.ofPattern("MMM d"))}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getMotivationalMessage(streak: Int, periodType: PeriodType): String {
    return when (periodType) {
        PeriodType.DAY -> when {
            streak == 0 -> "Every journey begins with a single step. Start today! 💪"
            streak == 1 -> "Day one complete! The hardest part is behind you."
            streak in 2..6 -> "You're building momentum! $streak days of dedication."
            streak == 7 -> "One week strong! You're proving your commitment. 🔥"
            streak in 8..13 -> "Almost two weeks! Consistency is becoming your superpower."
            streak == 14 -> "Two weeks! You're turning effort into habit. Keep going! 🚀"
            streak in 15..20 -> "Three weeks approaching! Your discipline is inspiring."
            streak in 21..29 -> "You're in the habit zone! This is who you are now. 💯"
            streak == 30 -> "30 days! A full month of dedication. You're unstoppable! 🏆"
            streak in 31..59 -> "Over a month strong! Your consistency is remarkable."
            streak in 60..89 -> "Two months of discipline! You're an inspiration. 🌟"
            streak in 90..99 -> "90+ days! You've built something truly special."
            streak == 100 -> "100 DAYS! You're a living legend! 👑"
            streak in 101..364 -> "${streak} days of excellence! Nothing can stop you now."
            streak >= 365 -> "ONE YEAR STREAK! Your dedication is absolutely extraordinary! 🎉👑"
            else -> "Keep pushing forward! 💪"
        }
        PeriodType.WEEK -> when {
            streak == 0 -> "Ready to start your journey? This week can be week one! 💪"
            streak == 1 -> "One week down! You've planted the seed of consistency."
            streak in 2..3 -> "$streak weeks of showing up! You're building something real."
            streak == 4 -> "One month! You're proving this isn't just motivation, it's discipline. 🔥"
            streak in 5..7 -> "Over a month! Your commitment is becoming unshakeable."
            streak == 8 -> "Two months of consistency! You're in the elite minority. 🚀"
            streak in 9..11 -> "Nearly three months! Your dedication speaks volumes."
            streak == 12 -> "12 weeks! A full quarter of unwavering commitment. 🏆"
            streak in 13..25 -> "${streak} weeks strong! You're rewriting what's possible."
            streak == 26 -> "Half a year! Your consistency is absolutely inspiring! 🌟"
            streak in 27..51 -> "Over 6 months! You're a beacon of dedication."
            streak == 52 -> "ONE YEAR STREAK! You've achieved what most only dream of! 👑🎉"
            streak in 53..103 -> "${streak} weeks of excellence! You're legendary."
            streak >= 104 -> "TWO YEARS! Your commitment is beyond extraordinary! 🏆👑⭐"
            else -> "Keep going! Every week counts! 💪"
        }
    }
}