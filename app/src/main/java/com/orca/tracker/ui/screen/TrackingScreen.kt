package com.orca.tracker.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.orca.tracker.ui.viewmodel.TrackingViewModel

@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel = hiltViewModel()
) {
    val trackingState by viewModel.trackingState.collectAsState()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            trackingState.currentLocation ?: LatLng(0.0, 0.0),
            15f
        )
    }

    LaunchedEffect(trackingState.currentLocation) {
        trackingState.currentLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Map Section (60% height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                if (trackingState.polyline.isNotEmpty()) {
                    Polyline(
                        points = trackingState.polyline,
                        color = androidx.compose.ui.graphics.Color.Blue,
                        width = 10f
                    )
                }

                trackingState.currentLocation?.let {
                    Marker(
                        state = rememberMarkerState(position = it),
                        title = "Current Location"
                    )
                }
            }
        }

        // Stats Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Speedometer
            Text(
                text = "%.1f km/h".format(trackingState.currentSpeed * 3.6),
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard("Distance", "%.2f km".format(trackingState.distance / 1000))
                StatCard("Duration", formatDuration(trackingState.duration))
                StatCard("Avg Speed", "%.1f km/h".format(trackingState.avgSpeed * 3.6))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (!trackingState.isTracking) {
                    Button(
                        onClick = { viewModel.startTracking() },
                        modifier = Modifier.size(80.dp)
                    ) {
                        Text("Start")
                    }
                } else {
                    if (trackingState.isPaused) {
                        Button(
                            onClick = { viewModel.resumeTracking() },
                            modifier = Modifier.size(80.dp)
                        ) {
                            Text("Resume")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.pauseTracking() },
                            modifier = Modifier.size(80.dp)
                        ) {
                            Text("Pause")
                        }
                    }

                    Button(
                        onClick = { viewModel.stopTracking() },
                        modifier = Modifier.size(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Stop")
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(text = value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = millis / (1000 * 60 * 60)
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}