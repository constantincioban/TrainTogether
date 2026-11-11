package com.konovus.traintogether.presentation.ui.screens.statsScreen.components

import android.graphics.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.konovus.traintogether.presentation.ui.screens.statsScreen.StatsEvent
import com.konovus.traintogether.presentation.ui.screens.statsScreen.StatsState
import com.konovus.traintogether.presentation.ui.screens.statsScreen.TimeFrame
import com.konovus.traintogether.presentation.ui.screens.statsScreen.VolumeDataPoint


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeProgressChartSection(
    uiState: StatsState,
    onEvent: (StatsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VOLUME PROGRESS",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${uiState.totalVolume} kg",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFFFA726)
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.FitnessCenter,
                    contentDescription = null,
                    tint = Color(0xFFFFA726).copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Exercise Dropdown
            ExposedDropdownMenuBox(
                expanded = uiState.isExerciseDropdownExpanded,
                onExpandedChange = { onEvent(StatsEvent.ToggleExerciseDropdown) }
            ) {
                OutlinedTextField(
                    value = uiState.selectedExercise.displayName,
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = uiState.isExerciseDropdownExpanded
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFA726),
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedTrailingIconColor = Color(0xFFFFA726),
                        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = uiState.isExerciseDropdownExpanded,
                    onDismissRequest = { onEvent(StatsEvent.ToggleExerciseDropdown) },
                    modifier = Modifier.background(Color(0xFF2C2C2E))
                ) {
                    uiState.allExercisesDoneByUser.forEach { exercise ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = exercise.displayName,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            },
                            onClick = {
                                onEvent(StatsEvent.OnExerciseSelected(exercise))
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                factory = { context ->
                    BarChart(context).apply {
                        setupVolumeChart(this, uiState.selectedTimeframe)
                    }
                },
                update = { chart ->
                    updateVolumeChart(chart, uiState.volumeData)
                }
            )
        }
    }
}

private fun setupVolumeChart(chart: BarChart, timeframe: TimeFrame) {
    chart.apply {
        description.isEnabled = false
        setTouchEnabled(true)
        setDragEnabled(true)
        setScaleEnabled(false) // Disable scaling, only allow scrolling
        setPinchZoom(false)
        setDrawBarShadow(false)
        setDrawValueAboveBar(true) // Show values above bars by default
        setDrawGridBackground(false)

        // Enable horizontal scrolling
        isDoubleTapToZoomEnabled = false

        // Styling
        setBackgroundColor(Color.Transparent.toArgb())

        // Add click listener for bars
        setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e != null) {
                    highlightValue(h)
                    invalidate()
                }
            }

            override fun onNothingSelected() {
                highlightValues(null)
                invalidate()
            }
        })

        // X Axis
        xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            textColor = Color.White.toArgb()
            textSize = 10f
            labelRotationAngle = -45f
            setLabelCount(6, false) // Show max 6 labels
            setAvoidFirstLastClipping(true)
        }

        // Y Axis
        axisLeft.apply {
            setDrawGridLines(true)
            gridColor = Color.White.copy(alpha = 0.1f).toArgb()
            textColor = Color.White.toArgb()
            textSize = 10f
            axisMinimum = 0f
        }

        axisRight.isEnabled = false
        legend.isEnabled = false

        animateY(1000)
    }
}

private fun updateVolumeChart(chart: BarChart, data: List<VolumeDataPoint>) {
    if (data.isEmpty()) {
        chart.clear()
        return
    }

    val entries = data.mapIndexed { index, dataPoint ->
        BarEntry(index.toFloat(), dataPoint.volume.toFloat())
    }

    val dataSet = BarDataSet(entries, "Volume").apply {
        color = Color(0xFFFFA726).toArgb()
        valueTextColor = Color.White.toArgb()
        valueTextSize = 12f
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value > 0) "${value.toInt()} kg" else ""
            }
        }
        setDrawValues(true)
        highLightColor = Color(0xFFFFD700).toArgb()
    }

    chart.renderer = object : BarChartRenderer(chart, chart.animator, chart.viewPortHandler) {
        override fun drawValue(
            canvas: Canvas,
            valueText: String,
            x: Float,
            y: Float,
            color: Int
        ) {
            val offsetY = y - 20f
            super.drawValue(canvas, valueText, x, offsetY, color)
        }
    }

    val barData = BarData(dataSet).apply {
        barWidth = 0.7f
    }

    chart.apply {
        this.data = barData
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val index = value.toInt()
                return if (index >= 0 && index < data.size) {
                    data[index].label
                } else ""
            }
        }

        // Set visible range to show max 6 bars at a time
        setVisibleXRangeMaximum(6f)

        // Move to the end (most recent workouts)
        if (data.size > 6) {
            moveViewToX((data.size - 6).toFloat())
        }

        invalidate()
    }
}
