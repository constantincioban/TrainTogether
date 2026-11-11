package com.konovus.traintogether.presentation.ui.screens.homeScreen.components


import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.imageLoader
import coil.util.DebugLogger
import com.konovus.traintogether.data.utils.PeriodType


@Composable
fun ProfilePopupDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    userName: String,
    userEmail: String,
    totalWorkouts: Int,
    currentStreak: Int,
    streakPeriodType: PeriodType = PeriodType.WEEK,
    totalVolume: String = "",
    profilePicUri: String = "",
    onViewProfile: () -> Unit = {},
    onViewSettings: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {

    var selectedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }
    LaunchedEffect(key1 = profilePicUri) {
        selectedImageUri = if (profilePicUri.isNotEmpty()) {
            profilePicUri.toUri()
        } else {
            null
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {},
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        ) {
                            if (selectedImageUri != null) {

                                val imageLoader = LocalContext.current.imageLoader.newBuilder()
                                    .logger(DebugLogger())
                                    .build()
                                AsyncImage(
                                    model = selectedImageUri,
                                    imageLoader = imageLoader,
                                    contentDescription = "Profile picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .border(
                                            2.dp,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            CircleShape
                                        )
                                        .align(Alignment.Center)
                                )
                            } else {
                                // Show default icon when no profile picture
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = "Default profile",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(28.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }

                        // User Info
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = userEmail,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                color = Color.Gray.copy(alpha = 0.8f)
                            )
                        }

                }
                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            value = totalWorkouts.toString(),
                            label = if (totalWorkouts == 1) "Workout" else "Workouts",
                        )

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(Color.Gray.copy(alpha = 0.3f))
                        )

                        StatItem(
                            value = totalVolume,
                            label = "Total Volume"
                        )

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(Color.Gray.copy(alpha = 0.3f))
                        )

                        StatItem(
                            value = "$currentStreak 🔥",
                            label = "${streakPeriodType.displayName} Streak"
                        )


                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    HorizontalDivider(
                        color = Color.Gray.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Menu Items
                    ProfileMenuItem(
                        icon = Icons.Filled.Person,
                        text = "Training Profile",
                        onClick = {
                            onViewProfile()
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalDivider(
                        color = Color.Gray.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ProfileMenuItem(
                        icon = Icons.Filled.Settings,
                        text = "Settings",
                        onClick = {
                            onViewSettings()
                            onDismiss()
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    HorizontalDivider(
                        color = Color.Gray.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ProfileMenuItem(
                        icon = Icons.Outlined.Logout,
                        text = "Sign Out",
                        textColor = MaterialTheme.colorScheme.error,
                        iconTint = MaterialTheme.colorScheme.error,
                        onClick = {
                            onSignOut()
                        }
                    )
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    text: String,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    iconTint: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = textColor
            )
        }
    }
}