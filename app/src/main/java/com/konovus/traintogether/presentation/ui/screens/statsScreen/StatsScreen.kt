package com.konovus.traintogether.presentation.ui.screens.statsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.konovus.traintogether.presentation.ui.screens.statsScreen.components.ExerciseDistributionPieChartSection
import com.konovus.traintogether.presentation.ui.screens.statsScreen.components.PersonalRecordsSection
import com.konovus.traintogether.presentation.ui.screens.statsScreen.components.QuickStatCard
import com.konovus.traintogether.presentation.ui.screens.statsScreen.components.TimePeriodDropdown
import com.konovus.traintogether.presentation.ui.screens.statsScreen.components.VolumeProgressChartSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    navigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.value
    val sendEvent = remember { viewModel::onEvent }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 102.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(
                        modifier = Modifier
                            .size(40.dp)

                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "STATISTICS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.5.sp
                            ),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Your Progress",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.95f)
                        )
                    }

                    // Time Period Filter
                    TimePeriodDropdown(
                        selectedPeriod = uiState.selectedTimeframe,
                        onPeriodSelected = { sendEvent(StatsEvent.OnTimeframeSelected(it)) }
                    )
                }
            }

            // Quick Stats Cards Row
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.quickStats) { stat ->
                    QuickStatCard(
                        stat = stat,
                        onClick = {  }
                    )
                }
            }

            VolumeProgressChartSection(
                uiState = uiState,
                onEvent = { sendEvent(it) }
            )

            ExerciseDistributionPieChartSection(
                exerciseCount = uiState.exerciseCount,
                exerciseDistribution = uiState.exerciseDistribution,
            )

            PersonalRecordsSection(
                personalRecords = uiState.personalRecords,
                onPersonalRecordClicked = { }
            )

        }
    }
}

