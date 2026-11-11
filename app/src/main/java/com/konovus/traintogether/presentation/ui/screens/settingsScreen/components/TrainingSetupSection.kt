package com.konovus.traintogether.presentation.ui.screens.settingsScreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.konovus.traintogether.data.local.models.PersonalRecord
import com.konovus.traintogether.data.utils.WorkoutFrequency
import kotlin.collections.forEach


@Composable
fun TrainingSetupSection(
    workoutFrequency: WorkoutFrequency,
    exercisePRs: List<PersonalRecord>,
    onFrequencyChange: (WorkoutFrequency) -> Unit,
    onAddPR: () -> Unit,
    onRemovePR: (PersonalRecord) -> Unit
) {
    SectionCard(title = "TRAINING SETUP") {
        // Workout Frequency Selection
        Text(
            text = "Workout Frequency",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            items(WorkoutFrequency.entries.toTypedArray()) { frequency ->
                FrequencyChip(
                    frequency = frequency,
                    isSelected = frequency == workoutFrequency,
                    onClick = { onFrequencyChange(frequency) }
                )
            }
        }

        // Exercise PRs Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Personal Records",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
            )

            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onAddPR() },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add PR",
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (exercisePRs.isEmpty()) {
            EmptyPRState(onAddPR = onAddPR)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(160.dp)
            ) {
                items(exercisePRs, key = { it.id}) { pr ->
                    PRItem(
                        exercisePR = pr,
                        onRemove = { onRemovePR(pr) }
                    )
                }
            }
        }
    }
}
