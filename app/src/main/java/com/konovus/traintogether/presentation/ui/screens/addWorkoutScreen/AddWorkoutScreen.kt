package com.konovus.traintogether.presentation.ui.screens.addWorkoutScreen

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.konovus.traintogether.data.local.models.Exercise
import com.konovus.traintogether.data.utils.GymExercise
import com.konovus.traintogether.data.utils.observeWithLifecycle
import com.konovus.traintogether.navigation.Screens
import com.konovus.traintogether.presentation.ui.screens.addWorkoutScreen.components.ExerciseConfigurationSection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddWorkoutScreen(
    workoutId: Int = 0,
    snackbarHostState: SnackbarHostState,
    navigateBack: () -> Unit,
    navigateTo: (route: String, params: List<Any>) -> Unit,
    navigateAndClearStack: (route: String, params: List<Any>) -> Unit,
    viewModel: AddWorkoutViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.value

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved)
            navigateAndClearStack(Screens.Home.route, emptyList())
    }

    viewModel.eventChannel.observeWithLifecycle {
        snackbarHostState.showSnackbar(message = it)
    }

    LaunchedEffect(uiState.navigateBack) {
        if (!uiState.navigateBack) return@LaunchedEffect
        println("${uiState.navigateBack} , ${uiState.workoutId}")
        if (uiState.workoutId != null && uiState.workoutId != 0) {
            navigateTo(Screens.Workout.route, listOf(workoutId))
        } else navigateBack()
    }



    Scaffold(
        floatingActionButton = {
            Box(
            ) {
                // Align the glow and FAB on top of each other
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.Center)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            CircleShape
                        )
                        .blur(8.dp)
                )

                FloatingActionButton(
                    onClick = { viewModel.onEvent(AddWorkoutEvent.SaveWorkout) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Save Workout",
                        modifier = Modifier.size(24.dp)
                    )
                }

            }
        }
    ) {
        Content(
            uiState = uiState,
            sendEvent = viewModel::onEvent,
            onBack = { viewModel.onEvent(AddWorkoutEvent.OnNavigateBack) },
            onDelete = {
                viewModel.onEvent(AddWorkoutEvent.DeleteWorkout)
                navigateAndClearStack("home", emptyList())
            }
        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun Content(
    uiState: AddWorkoutState,
    sendEvent: (event: AddWorkoutEvent) -> Unit,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        // Custom Top Bar
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }


                // Title
                Text(
                    text = if (uiState.workoutId != null) "Edit Workout" else "New Workout",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                )

                // Delete Button
                if (uiState.workoutId != null)
                    Surface(
                        onClick = onDelete,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                else Spacer(Modifier.width(40.dp))
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Workout Title Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color.Black.copy(alpha = 0.2f)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Workout Details",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.25.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { sendEvent(AddWorkoutEvent.OnTitleChanged(it)) },
                        placeholder = {
                            Text(
                                "Enter workout title",
                                color = Color.Gray.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Exercise Selection Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = Color.Black.copy(alpha = 0.2f)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Choose Exercises",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.25.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${uiState.exercises.size}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    ExercisePager(
                        selectedExercises = uiState.exercises.map { it.gymExercise },
                        sendEvent = sendEvent
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Exercise Configuration Section
            ExerciseConfigurationSection(
                exercises = uiState.exercises,
                sendEvent = sendEvent,
                circuitExerciseButtonPositions = uiState.circuitExerciseButtonPositions
            )
            // Bottom spacing for FAB
            Spacer(modifier = Modifier.height(130.dp))
        }
    }
}


@Composable
fun ExerciseRow(
    exercise: GymExercise,
    warmUpSets: String,
    workingSets: String,
    sendEvent: (event: AddWorkoutEvent) -> Unit,
    index: Int,
    circuitButtonsPositions: List<Int> = emptyList(),
    exercises: List<Exercise> = emptyList(),
) {
    val focusManager = LocalFocusManager.current
    val warmupBringIntoView = remember { BringIntoViewRequester() }
    val workingBringIntoView = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    val isCircuitConnector = circuitButtonsPositions.contains(index)
    val isConnectedToCircuit = circuitButtonsPositions.contains(index - 1)
    val isPartOfCircuit = isCircuitConnector || isConnectedToCircuit

    val circuitGroup = findCircuitGroup(index, circuitButtonsPositions)
    val isFirstInCircuit = circuitGroup?.first() == index
    val isLastInCircuit = circuitGroup?.last() == index
    val isMiddleInCircuit = circuitGroup != null && !isFirstInCircuit && !isLastInCircuit

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isPartOfCircuit) {
            if (isFirstInCircuit) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Repeat,
                                contentDescription = "Circuit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CIRCUIT ${circuitGroup?.size ?: 0} EXERCISES",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Ungroup button - orange accent is intentional branding, kept as-is
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(color = Color.Transparent, shape = CircleShape)
                                .clickable {
                                    circuitGroup?.let { group ->
                                        val buttonsToToggle =
                                            circuitButtonsPositions.filter { buttonIndex ->
                                                val buttonGroup = findCircuitGroup(
                                                    buttonIndex,
                                                    circuitButtonsPositions
                                                )
                                                buttonGroup?.any { it in group } == true
                                            }
                                        buttonsToToggle.forEach { buttonIndex ->
                                            sendEvent(
                                                AddWorkoutEvent.OnToggleCircuitExerciseButton(buttonIndex)
                                            )
                                        }
                                    }
                                }
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(color = Color.Transparent, shape = CircleShape)
                                    .drawBehind {
                                        drawCircle(
                                            color = Color(0xFFFF9500),
                                            style = Stroke(width = 1.dp.toPx())
                                        )
                                    }
                            )
                            Text(
                                text = "✕",
                                color = Color(0xFFFF9500),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Main exercise row
        Surface(
            color = if (isPartOfCircuit) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            shape = when {
                isPartOfCircuit && isFirstInCircuit && isLastInCircuit -> RoundedCornerShape(12.dp)
                isPartOfCircuit && isFirstInCircuit -> RoundedCornerShape(0.dp)
                isPartOfCircuit && isLastInCircuit -> RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                isPartOfCircuit -> RoundedCornerShape(0.dp)
                else -> RoundedCornerShape(12.dp)
            },
            border = if (isPartOfCircuit) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            } else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (isPartOfCircuit) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                },
                                when {
                                    isFirstInCircuit -> RoundedCornerShape(
                                        topStart = 4.dp, topEnd = 4.dp,
                                        bottomStart = 2.dp, bottomEnd = 2.dp
                                    )
                                    isLastInCircuit -> RoundedCornerShape(
                                        topStart = 2.dp, topEnd = 2.dp,
                                        bottomStart = 4.dp, bottomEnd = 4.dp
                                    )
                                    isMiddleInCircuit -> RoundedCornerShape(2.dp)
                                    else -> CircleShape
                                }
                            )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = exercise.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.25.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isPartOfCircuit && circuitGroup != null) {
                            Text(
                                text = "Exercise ${circuitGroup.indexOf(index) + 1} of ${circuitGroup.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Warm-up sets input
                OutlinedTextField(
                    value = warmUpSets,
                    onValueChange = { sendEvent(AddWorkoutEvent.OnChangeWarmUpSets(it, exercise)) },
                    singleLine = true,
                    placeholder = {
                        Text(
                            "0",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                    ),
                    modifier = Modifier
                        .width(80.dp)
                        .bringIntoViewRequester(warmupBringIntoView)
                        .onFocusChanged {
                            if (it.isFocused) {
                                coroutineScope.launch {
                                    delay(200)
                                    warmupBringIntoView.bringIntoView()
                                }
                            }
                        },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Working sets input
                OutlinedTextField(
                    value = workingSets,
                    onValueChange = { sendEvent(AddWorkoutEvent.OnChangeWorkingSets(it, exercise)) },
                    singleLine = true,
                    placeholder = {
                        Text(
                            "0",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                    ),
                    modifier = Modifier
                        .width(80.dp)
                        .bringIntoViewRequester(workingBringIntoView)
                        .onFocusChanged {
                            if (it.isFocused) {
                                coroutineScope.launch {
                                    delay(200)
                                    workingBringIntoView.bringIntoView()
                                }
                            }
                        },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Circuit toggle button
        if (exercises.size >= index + 2) {
            val buttonSpaceHeight by animateFloatAsState(
                targetValue = if (isCircuitConnector) 0f else 56f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "button_space_collapse"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(buttonSpaceHeight.dp),
                contentAlignment = Alignment.Center
            ) {
                val buttonOffset by animateFloatAsState(
                    targetValue = if (isCircuitConnector) -28f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "button_position"
                )

                Surface(
                    onClick = { sendEvent(AddWorkoutEvent.OnToggleCircuitExerciseButton(index)) },
                    shape = CircleShape,
                    color = if (isCircuitConnector) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    border = if (!isCircuitConnector) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    } else null,
                    modifier = Modifier
                        .size(32.dp)
                        .offset(y = buttonOffset.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (isCircuitConnector) Icons.Filled.Done else Icons.Outlined.Repeat,
                            contentDescription = if (isCircuitConnector) "Remove from Circuit" else "Add to Circuit",
                            tint = if (isCircuitConnector) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

// Helper function to find circuit groups
fun findCircuitGroup(index: Int, circuitButtonsPositions: List<Int>): List<Int>? {
    if (circuitButtonsPositions.isEmpty()) return null

    // Create individual circuit pairs first
    val circuitPairs = circuitButtonsPositions.map { buttonIndex ->
        listOf(buttonIndex, buttonIndex + 1)
    }

    // Merge pairs only if they share exercises (overlap)
    val mergedGroups = mutableListOf<List<Int>>()

    for (pair in circuitPairs) {
        var merged = false

        // Try to merge with existing groups only if they overlap
        for (i in mergedGroups.indices) {
            val existingGroup = mergedGroups[i]

            // Only merge if there's actual overlap (shared exercise)
            if (pair.any { it in existingGroup }) {
                // Merge the pair with this group
                mergedGroups[i] = (existingGroup + pair).distinct().sorted()
                merged = true
                break
            }
        }

        // If not merged, create new group
        if (!merged) {
            mergedGroups.add(pair)
        }
    }

    // Continue merging overlapping groups
    var changed = true
    while (changed) {
        changed = false
        for (i in mergedGroups.indices) {
            for (j in i + 1 until mergedGroups.size) {
                val group1 = mergedGroups[i]
                val group2 = mergedGroups[j]

                // Only merge if groups share exercises (overlap)
                if (group1.any { it in group2 }) {
                    // Merge groups
                    mergedGroups[i] = (group1 + group2).distinct().sorted()
                    mergedGroups.removeAt(j)
                    changed = true
                    break
                }
            }
            if (changed) break
        }
    }

    // Find which group contains this index
    return mergedGroups.find { it.contains(index) }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun ExercisePager(
    selectedExercises: List<GymExercise>,
    sendEvent: (event: AddWorkoutEvent) -> Unit,
) {
    val exercisesPerPage = 9
    val pages = GymExercise.entries.chunked(exercisesPerPage)
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.height(200.dp)
        ) { pageIndex ->
            val pageExercises = pages[pageIndex]
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false,
                contentPadding = PaddingValues(8.dp)
            ) {
                items(pageExercises.size) { index ->
                    val exercise = pageExercises[index]
                    val isSelected = selectedExercises.contains(exercise)

                    Surface(
                        onClick = { sendEvent(AddWorkoutEvent.OnExerciseToggled(exercise)) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        border = if (isSelected) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = exercise.displayName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    letterSpacing = 0.25.sp
                                ),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                },
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Page indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (selected) 8.dp else 6.dp)
                        .background(
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            },
                            shape = CircleShape
                        )
                        .animateContentSize()
                )
            }
        }
    }
}