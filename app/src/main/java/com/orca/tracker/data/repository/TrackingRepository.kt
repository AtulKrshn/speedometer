package com.orca.tracker.data.repository


import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.orca.tracker.data.dao.LocationPointDao
import com.orca.tracker.data.dao.RunDao
import com.orca.tracker.data.entity.LocationPointEntity
import com.orca.tracker.data.entity.RunEntity
import com.orca.tracker.data.model.TrackingState
import com.orca.tracker.util.PolylineUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class TrackingRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val runDao: RunDao,
    private val locationPointDao: LocationPointDao
) {
    private val _trackingState = MutableStateFlow(TrackingState())
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private var startTime: Long = 0L
    private var pausedTime: Long = 0L
    private var totalPausedDuration: Long = 0L
    private val polylinePoints = mutableListOf<LatLng>()
    private var lastLocation: LatLng? = null
    private var totalDistance = 0.0
    private var maxSpeed = 0.0
    private var speedSum = 0.0
    private var speedCount = 0

    suspend fun startTracking(runId: String) {
        startTime = System.currentTimeMillis()
        totalPausedDuration = 0L
        polylinePoints.clear()
        lastLocation = null
        totalDistance = 0.0
        maxSpeed = 0.0
        speedSum = 0.0
        speedCount = 0

        val run = RunEntity(
            id = runId,
            startTime = startTime,
            isActive = true
        )
        runDao.insertRun(run)

        _trackingState.value = _trackingState.value.copy(
            isTracking = true,
            isPaused = false,
            runId = runId
        )
    }

    fun pauseTracking() {
        pausedTime = System.currentTimeMillis()
        _trackingState.value = _trackingState.value.copy(isPaused = true)
    }

    fun resumeTracking() {
        if (pausedTime > 0) {
            totalPausedDuration += System.currentTimeMillis() - pausedTime
            pausedTime = 0L
        }
        _trackingState.value = _trackingState.value.copy(isPaused = false)
    }

    suspend fun stopTracking(): RunEntity? {
        val runId = _trackingState.value.runId ?: return null
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime - totalPausedDuration

        val run = runDao.getRunById(runId)?.copy(
            endTime = endTime,
            duration = duration,
            distanceMeters = totalDistance,
            avgSpeed = if (speedCount > 0) speedSum / speedCount else 0.0,
            maxSpeed = maxSpeed,
            encodedPolyline = PolylineUtil.encode(polylinePoints),
            isActive = false
        )

        run?.let { runDao.updateRun(it) }

        _trackingState.value = TrackingState()
        polylinePoints.clear()

        return run
    }

    suspend fun addLocationUpdate(
        latitude: Double,
        longitude: Double,
        speed: Float,
        accuracy: Float,
        altitude: Double
    ) {
        val state = _trackingState.value
        if (!state.isTracking || state.isPaused) return

        // Filter inaccurate points
        if (accuracy > 40f) return

        val newLocation = LatLng(latitude, longitude)
        val runId = state.runId ?: return

        // Save to database
        locationPointDao.insertPoint(
            LocationPointEntity(
                runId = runId,
                latitude = latitude,
                longitude = longitude,
                altitude = altitude,
                accuracy = accuracy,
                speed = speed,
                timestamp = System.currentTimeMillis()
            )
        )

        // Calculate distance if we have a previous point
        lastLocation?.let { last ->
            val distance = calculateDistance(last, newLocation)

            // Filter GPS jumps (>100m in one update is suspicious)
            if (distance < 100.0) {
                totalDistance += distance
                polylinePoints.add(newLocation)
            }
        } ?: run {
            polylinePoints.add(newLocation)
        }

        lastLocation = newLocation

        // Update speed stats
        val speedMps = speed.toDouble()
        if (speedMps > maxSpeed) maxSpeed = speedMps
        speedSum += speedMps
        speedCount++

        val avgSpeed = if (speedCount > 0) speedSum / speedCount else 0.0
        val duration = if (pausedTime > 0) {
            pausedTime - startTime - totalPausedDuration
        } else {
            System.currentTimeMillis() - startTime - totalPausedDuration
        }

        _trackingState.value = state.copy(
            currentSpeed = speedMps,
            distance = totalDistance,
            duration = duration,
            avgSpeed = avgSpeed,
            maxSpeed = maxSpeed,
            polyline = polylinePoints.toList(),
            currentLocation = newLocation,
            gpsAccuracy = accuracy
        )
    }

    suspend fun saveCheckpoint() {
        val state = _trackingState.value
        val runId = state.runId ?: return

        val duration = if (state.isPaused && pausedTime > 0) {
            pausedTime - startTime - totalPausedDuration
        } else {
            System.currentTimeMillis() - startTime - totalPausedDuration
        }

        val run = runDao.getRunById(runId)?.copy(
            duration = duration,
            distanceMeters = totalDistance,
            avgSpeed = state.avgSpeed,
            maxSpeed = maxSpeed,
            encodedPolyline = PolylineUtil.encode(polylinePoints)
        )

        run?.let { runDao.updateRun(it) }
    }

    suspend fun recoverActiveRun() {
        val activeRun = runDao.getActiveRun() ?: return
        val points = locationPointDao.getPointsForRun(activeRun.id)

        if (points.isEmpty()) return

        startTime = activeRun.startTime
        totalDistance = activeRun.distanceMeters
        maxSpeed = activeRun.maxSpeed

        polylinePoints.clear()
        polylinePoints.addAll(PolylineUtil.decode(activeRun.encodedPolyline))

        if (polylinePoints.isNotEmpty()) {
            lastLocation = polylinePoints.last()
        }

        speedSum = activeRun.avgSpeed * points.size
        speedCount = points.size

        _trackingState.value = TrackingState(
            isTracking = true,
            isPaused = false,
            distance = totalDistance,
            duration = activeRun.duration,
            avgSpeed = activeRun.avgSpeed,
            maxSpeed = maxSpeed,
            polyline = polylinePoints.toList(),
            currentLocation = lastLocation,
            runId = activeRun.id
        )
    }

    private fun calculateDistance(start: LatLng, end: LatLng): Double {
        val earthRadius = 6371000.0 // meters

        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val deltaLat = Math.toRadians(end.latitude - start.latitude)
        val deltaLng = Math.toRadians(end.longitude - start.longitude)

        val a = sin(deltaLat / 2).pow(2) +
                cos(lat1) * cos(lat2) *
                sin(deltaLng / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }
}