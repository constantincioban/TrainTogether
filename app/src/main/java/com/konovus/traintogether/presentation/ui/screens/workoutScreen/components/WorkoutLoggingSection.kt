package com.konovus.traintogether.presentation.ui.screens.workoutScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.konovus.traintogether.presentation.ui.screens.workoutScreen.WorkoutEvent
import com.konovus.traintogether.presentation.ui.screens.workoutScreen.WorkoutEvent.OnUpdateReps

@Composable
fun WorkoutLoggingSection(
    reps: Int,
    weight: Int,
    sendEvent: (WorkoutEvent) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "SET LOG",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                ModernNumberPicker(
                    label = "Reps",
                    range = 1..99,
                    selected = reps,
                    onValueChange = { sendEvent(OnUpdateReps(it)) },
                    color = MaterialTheme.colorScheme.primary
                )

                ModernNumberPicker(
                    label = "Weight",
                    range = 0..999,
                    selected = weight,
                    onValueChange = { sendEvent(WorkoutEvent.OnUpdateWeight(it)) },
                    color = Color(0xFFFFA726),
                    suffix = "kg"
                )
            }
        }
    }

}