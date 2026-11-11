package com.konovus.traintogether.presentation.ui.screens.homeScreen.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konovus.traintogether.data.utils.ImmutableList
import com.konovus.traintogether.data.utils.ImmutableMap
import com.konovus.traintogether.presentation.ui.screens.homeScreen.HomeEvents
import com.konovus.traintogether.presentation.ui.screens.homeScreen.TimeFrame
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProgressSection(
    timeFrame: TimeFrame = TimeFrame.WEEK,
    currentTimeOffset: Int = 0, // Changed from local state
    checkedDays: ImmutableList<LocalDate> = ImmutableList(),
    monthlyWorkouts: ImmutableMap<Int, Int> = ImmutableMap(),
    totalWorkoutLogsForTimeframe: Int = 0,
    totalWeightForTimeframe: String = "0kg",
    totalTime: String = "00:00",
    sendEvent: (HomeEvents) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Progress",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                TimeFrameDropdown(
                    selected = timeFrame,
                    onSelected = {
                        sendEvent(HomeEvents.OnUpdateTimeFrame(it))
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Swipeable content
            SwipeableTimeView(
                timeFrame = timeFrame,
                currentOffset = currentTimeOffset,
                checkedDays = checkedDays,
                monthlyWorkouts = monthlyWorkouts,
                sendEvent = sendEvent,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = totalWorkoutLogsForTimeframe.toString(),
                    label = if (totalWorkoutLogsForTimeframe == 1) "Workout" else "Workouts",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    value = totalWeightForTimeframe,
                    label = "Total Weight",
                    color = Color(0xFFFFA726)
                )
                StatItem(
                    value = totalTime,
                    label = "Time",
                    color = Color(0xFF66BB6A)
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SwipeableTimeView(
    timeFrame: TimeFrame,
    currentOffset: Int,
    checkedDays: List<LocalDate>,
    monthlyWorkouts: Map<Int, Int>,
    sendEvent: (HomeEvents) -> Unit
) {
    AnimatedContent(
        targetState = Pair(timeFrame, currentOffset),
        transitionSpec = {
            val (oldTimeFrame, oldOffset) = initialState
            val (newTimeFrame, newOffset) = targetState

            if (oldTimeFrame != newTimeFrame) {
                (fadeIn(animationSpec = tween(200)) + slideInVertically(
                    animationSpec = tween(200),
                    initialOffsetY = { it / 4 }
                )).togetherWith(
                    fadeOut(animationSpec = tween(150)) + slideOutVertically(
                        animationSpec = tween(150),
                        targetOffsetY = { -it / 4 }
                    )
                )
            } else {
                val slideDirection = if (newOffset > oldOffset) {
                    slideInHorizontally(
                        animationSpec = tween(250),
                        initialOffsetX = { it }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(250),
                        targetOffsetX = { -it }
                    )
                } else {
                    slideInHorizontally(
                        animationSpec = tween(250),
                        initialOffsetX = { -it }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(250),
                        targetOffsetX = { it }
                    )
                }
                slideDirection
            }
        }
    ) { (currentTimeFrame, offset) ->
        when (currentTimeFrame) {
            TimeFrame.WEEK -> SwipeableWeekView(
                checkedDays = checkedDays,
                offset = offset,
                sendEvent = sendEvent
            )

            TimeFrame.MONTH -> SwipeableMonthView(
                checkedDays = checkedDays,
                offset = offset,
                sendEvent = sendEvent
            )

            TimeFrame.YEAR -> SwipeableYearView(
                monthlyWorkouts = monthlyWorkouts,
                offset = offset,
                sendEvent = sendEvent
            )
        }
    }
}

@Composable
fun SwipeableWeekView(
    checkedDays: List<LocalDate>,
    offset: Int,
    sendEvent: (HomeEvents) -> Unit
) {
    val today = LocalDate.now()
    val weekStart = today.minusDays(6 + (abs(offset) * 7).toLong())
    val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }
    var totalDragDistance by remember { mutableStateOf(0f) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(offset) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        totalDragDistance = 0f
                    },
                    onDragEnd = {
                        val threshold = 100.dp.toPx()
                        if (abs(totalDragDistance) > threshold) {
                            if (totalDragDistance > 0) {
                                val newOffset = offset - 1
                                sendEvent(HomeEvents.OnTimePeriodsChanged(newOffset))
                            } else {
                                if (offset < 0) {
                                    val newOffset = offset + 1
                                    sendEvent(HomeEvents.OnTimePeriodsChanged(newOffset))
                                }
                            }
                        }
                        totalDragDistance = 0f
                    }
                ) { _, dragAmount ->
                    totalDragDistance += dragAmount
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newOffset = offset - 1
                    sendEvent(HomeEvents.OnTimePeriodsChanged(newOffset))
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Previous week",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            val weekStartDate = weekDays.first()
            val weekEndDate = weekDays.last()
            val dateRange = if (weekStartDate.month == weekEndDate.month) {
                "${weekStartDate.format(DateTimeFormatter.ofPattern("MMM d"))} - ${weekEndDate.dayOfMonth}"
            } else {
                "${weekStartDate.format(DateTimeFormatter.ofPattern("MMM d"))} - ${
                    weekEndDate.format(DateTimeFormatter.ofPattern("MMM d"))
                }"
            }

            Text(
                text = if (offset == 0) "Last 7 Days" else dateRange,
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )

            IconButton(
                onClick = {
                    if (offset < 0) {
                        val newOffset = offset + 1
                        sendEvent(HomeEvents.OnTimePeriodsChanged(newOffset))
                    }
                },
                modifier = Modifier.size(32.dp),
                enabled = offset < 0
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next week",
                    tint = if (offset < 0) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            weekDays.forEach { date ->
                val isChecked = checkedDays.contains(date)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("EEE")),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .then(
                                if (isChecked) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                ) else Modifier
                            )
                            .clickable{ sendEvent(HomeEvents.OnDateClicked(date)) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChecked) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeableMonthView(
    checkedDays: List<LocalDate>,
    offset: Int,
    sendEvent: (HomeEvents) -> Unit
) {
    val today = LocalDate.now()
    val targetMonth = YearMonth.from(today.plusMonths(offset.toLong()))
    val firstDayOfMonth = targetMonth.atDay(1)
    val startDate = firstDayOfMonth.minusDays(firstDayOfMonth.dayOfWeek.value.toLong() - 1)
    var totalDragDistance by remember { mutableStateOf(0f) }

    val calendarDays = (0..34).map { startDate.plusDays(it.toLong()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(offset) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        totalDragDistance = 0f
                    },
                    onDragEnd = {
                        val threshold = 100.dp.toPx()
                        if (abs(totalDragDistance) > threshold) {
                            if (totalDragDistance > 0) {
                                val newOffset = offset - 1
                                sendEvent(HomeEvents.OnTimePeriodsChanged(newOffset))
                            } else {
                                if (offset < 0) {
                                    val newOffset = offset + 1
                                    sendEvent(HomeEvents.OnTimePeriodsChanged(newOffset))
                                }
                            }
                        }
                        totalDragDistance = 0f
                    }
                ) { _, dragAmount ->
                    totalDragDistance += dragAmount
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newOffset = offset - 1
                    sendEvent(HomeEvents.OnTimePeriodsChanged(newOffset))
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Previous month",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = targetMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )

            IconButton(
                onClick = {
                    if (offset < 0) {
                        val newOffset = offset + 1
                        sendEvent(HomeEvents.OnTimePeriodsChanged(newOffset))
                    }
                },
                modifier = Modifier.size(32.dp),
                enabled = offset < 0
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next month",
                    tint = if (offset < 0) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        calendarDays.chunked(7).forEach { week ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                week.forEach { date ->
                    val isInCurrentMonth = date.month == targetMonth.month
                    val isChecked = checkedDays.contains(date) && isInCurrentMonth
                    val isToday = date == today

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isChecked -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    isToday -> Color.Gray.copy(alpha = 0.3f)
                                    else -> Color.Transparent
                                }
                            )
                            .then(
                                if (isChecked) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                ) else Modifier
                            )
                            .clickable{ sendEvent(HomeEvents.OnDateClicked(date)) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChecked) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = date.dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isInCurrentMonth) MaterialTheme.colorScheme.onBackground else Color.Gray.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SwipeableYearView(
    monthlyWorkouts: Map<Int, Int>,
    offset: Int,
    sendEvent: (HomeEvents) -> Unit
) {
    val currentYear = LocalDate.now().year + offset
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    var totalDragDistance by remember { mutableStateOf(0f) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(offset) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        totalDragDistance = 0f
                    },
                    onDragEnd = {
                        val threshold = 100.dp.toPx()
                        if (abs(totalDragDistance) > threshold) {
                            if (totalDragDistance > 0) {
                                val newOffset = offset - 1
                                sendEvent(HomeEvents.OnTimePeriodsChanged(newOffset))
                            } else {
                                if (offset < 0) {
                                    val newOffset = offset + 1
                                    sendEvent(HomeEvents.OnTimePeriodsChanged(newOffset))
                                }
                            }
                        }
                        totalDragDistance = 0f
                    }
                ) { _, dragAmount ->
                    totalDragDistance += dragAmount
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newOffset = offset - 1
                    sendEvent(HomeEvents.OnTimePeriodsChanged(newOffset))
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Previous year",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = currentYear.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )

            IconButton(
                onClick = {
                    if (offset < 0) {
                        val newOffset = offset + 1
                        sendEvent(HomeEvents.OnTimePeriodsChanged(newOffset))
                    }
                },
                modifier = Modifier.size(32.dp),
                enabled = offset < 0
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next year",
                    tint = if (offset < 0) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            (0..5).forEach { monthIndex ->
                MonthItem(
                    month = months[monthIndex],
                    workouts = monthlyWorkouts[monthIndex + 1] ?: 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            (6..11).forEach { monthIndex ->
                MonthItem(
                    month = months[monthIndex],
                    workouts = monthlyWorkouts[monthIndex + 1] ?: 0,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MonthItem(
    month: String,
    workouts: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = month,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (workouts > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .then(
                    if (workouts > 0) Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = workouts.toString(),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = if (workouts > 0) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun TimeFrameDropdown(
    selected: TimeFrame,
    onSelected: (TimeFrame) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = selected.name,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.25.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            TimeFrame.entries.forEach { timeFrame ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = timeFrame.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (timeFrame == selected) FontWeight.Medium else FontWeight.Normal
                            ),
                            color = if (timeFrame == selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelected(timeFrame)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}