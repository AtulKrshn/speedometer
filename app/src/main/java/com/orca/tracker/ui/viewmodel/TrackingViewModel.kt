package com.orca.tracker.ui.viewmodel


import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.orca.tracker.data.repository.TrackingRepository
import com.orca.tracker.service.TrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    application: Application,
    private val trackingRepository: TrackingRepository
) : AndroidViewModel(application) {

    val trackingState = trackingRepository.trackingState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        trackingRepository.trackingState.value
    )

    fun startTracking() {
        val runId = UUID.randomUUID().toString()
        val intent = Intent(getApplication(), TrackingService::class.java).apply {
            action = TrackingService.ACTION_START
            putExtra(TrackingService.EXTRA_RUN_ID, runId)
        }
        getApplication<Application>().startForegroundService(intent)
    }

    fun pauseTracking() {
        val intent = Intent(getApplication(), TrackingService::class.java).apply {
            action = TrackingService.ACTION_PAUSE
        }
        getApplication<Application>().startService(intent)
    }

    fun resumeTracking() {
        val intent = Intent(getApplication(), TrackingService::class.java).apply {
            action = TrackingService.ACTION_RESUME
        }
        getApplication<Application>().startService(intent)
    }

    fun stopTracking() {
        val intent = Intent(getApplication(), TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP
        }
        getApplication<Application>().startService(intent)
    }

    fun recoverTracking() {
        viewModelScope.launch {
            trackingRepository.recoverActiveRun()
        }
    }
}