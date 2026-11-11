package com.konovus.traintogether.presentation.ui.screens.statsScreen.components

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
import androidx.compose.material.icons.rounded.DonutSmall
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

@Composable
fun ExerciseDistributionPieChartSection(
    exerciseCount: Int,
    exerciseDistribution: List<PieEntry>,
) {
    Card(
        modifier = Modifier
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EXERCISE DISTRIBUTION",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$exerciseCount exercises",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF66BB6A)
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.DonutSmall,
                    contentDescription = null,
                    tint = Color(0xFF66BB6A).copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                factory = { context ->
                    PieChart(context).apply {
                        setupPieChart(this)
                    }
                },
                update = { chart ->
                    updatePieChart(chart, exerciseDistribution)
                }
            )
        }
    }

}

private fun setupPieChart(chart: PieChart) {
    chart.apply {
        setBackgroundColor(Color.Transparent.toArgb())
        description.isEnabled = false
        setDrawHoleEnabled(true)
        setHoleColor(Color.Transparent.toArgb())
        holeRadius = 40f
        transparentCircleRadius = 45f
        setDrawCenterText(true)
        centerText = "Exercises"
        setCenterTextColor(Color.White.toArgb())
        setCenterTextSize(14f)
        setEntryLabelColor(Color.White.toArgb())
        setEntryLabelTextSize(10f)
        legend.apply {
            isEnabled = true
            textColor = android.graphics.Color.parseColor("#80FFFFFF")
            textSize = 10f
            orientation = Legend.LegendOrientation.VERTICAL
            verticalAlignment = Legend.LegendVerticalAlignment.CENTER
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        }
        setTouchEnabled(true)

        // Add click listener for pie slices
        setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e is PieEntry && h != null) {
                    // Calculate percentage
                    val total = data?.yValueSum ?: 0f
                    val percentage = (e.value / total * 100).toInt()

                    // Show percentage in center text
                    centerText = "${e.label}"
                    setCenterTextSize(12f)

                    // Optionally show percentage on the slice itself
                    data?.dataSet?.setDrawValues(true)
                    data?.dataSet?.valueFormatter = object : ValueFormatter() {
                        override fun getPieLabel(value: Float, pieEntry: PieEntry?): String {
                            return if (pieEntry == e) "${percentage}%" else ""
                        }
                    }

                    invalidate()
                }
            }

            override fun onNothingSelected() {
                // Reset to default center text
                centerText = "Exercises"
                setCenterTextSize(14f)

                // Hide values on slices
                data?.dataSet?.setDrawValues(false)
                invalidate()
            }
        })

        animateY(800)
    }
}

private fun updatePieChart(chart: PieChart, data: List<PieEntry>) {
    val colors = listOf(
        android.graphics.Color.parseColor("#007AFF"),
        android.graphics.Color.parseColor("#FFA726"),
        android.graphics.Color.parseColor("#66BB6A"),
        android.graphics.Color.parseColor("#EF5350"),
        android.graphics.Color.parseColor("#AB47BC"),
        android.graphics.Color.parseColor("#26C6DA")
    )

    val dataSet = PieDataSet(data, "").apply {
        this.colors = colors
        sliceSpace = 2f
        setDrawValues(false) // Initially hide values
        selectionShift = 8f

        // Style for percentage text when shown
        valueTextColor = Color.White.toArgb()
        valueTextSize = 12f
    }

    chart.data = PieData(dataSet)
    chart.invalidate()
}
