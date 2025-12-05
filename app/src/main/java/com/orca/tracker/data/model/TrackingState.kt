package com.orca.tracker.data.model


import com.google.android.gms.maps.model.LatLng

data class TrackingState(
    val isTracking: Boolean = false,
    val isPaused: Boolean = false,
    val currentSpeed: Double = 0.0,
    val distance: Double = 0.0,
    val duration: Long = 0L,
    val avgSpeed: Double = 0.0,
    val maxSpeed: Double = 0.0,
    val polyline: List<LatLng> = emptyList(),
    val currentLocation: LatLng? = null,
    val runId: String? = null,
    val gpsAccuracy: Float = 0f,
    val isGpsEnabled: Boolean = true
)