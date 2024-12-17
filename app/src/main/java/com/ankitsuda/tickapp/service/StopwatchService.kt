package com.ankitsuda.tickapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.ankitsuda.tickapp.MainActivity
import com.ankitsuda.tickapp.R
import com.ankitsuda.tickapp.util.formatMillisToTimer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class StopwatchService : LifecycleService() {
    private lateinit var notificationManager: NotificationManager
    private var elapsedMillisBeforePause = 0L
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {
        super.onCreate()
        setupNotification()
        setupMediaPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                ServiceState.START_OR_RESUME.name -> {
                    startResumeStopWatch()
                    startForeground(
                        NOTIFICATION_ID,
                        getNotification(
                            getString(R.string.stopwatch_running),
                            formatMillisToTimer(elapsedMillisBeforePause)
                        )
                    )
                }

                ServiceState.PAUSE.name -> {
                    _isTracking.value = false
                    elapsedMillisBeforePause = _elapsedMilliSeconds.value!!
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        getNotification(
                            getString(R.string.stopwatch_running),
                            formatMillisToTimer(elapsedMillisBeforePause)
                        )
                    )
                }

                ServiceState.RESET.name -> {
                    resetStopWatch()
                    stopForeground(true)
                    stopSelf()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startResumeStopWatch() {
        _isTracking.value = true
        lifecycleScope.launch(Dispatchers.IO) {
            val startTimeMillis = System.currentTimeMillis()
            while (_isTracking.value!!) {
                _elapsedMilliSeconds.emit((System.currentTimeMillis() - startTimeMillis) + elapsedMillisBeforePause)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(_elapsedMilliSeconds.value!!)
                if (_elapsedSeconds.value != seconds) {
                    _elapsedSeconds.emit(seconds)
                    playSound()
                }
                delay(100)
            }
        }
    }

    private fun resetStopWatch() {
        _isTracking.value = false
        _elapsedMilliSeconds.value = 0L
        _elapsedSeconds.value = 0L
        elapsedMillisBeforePause = 0L
        lifecycleScope.coroutineContext.cancelChildren()
    }

    private fun setupNotification() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        lifecycleScope.launch {
            _elapsedSeconds.collectLatest { elapsedSeconds ->
                if (_isTracking.value) {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        getNotification(
                            getString(R.string.stopwatch_running),
                            formatMillisToTimer(TimeUnit.SECONDS.toMillis(elapsedSeconds))
                        )
                    )
                }
            }
        }
    }

    private fun getNotification(title: String, text: String): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setOngoing(true)
            .setAutoCancel(false)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    1,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                if (_isTracking.value!!) getString(R.string.pause) else getString(R.string.resume),
                PendingIntent.getService(
                    this,
                    2,
                    Intent(
                        this,
                        StopwatchService::class.java
                    ).also {
                        it.action =
                            if (_isTracking.value!!) ServiceState.PAUSE.name else ServiceState.START_OR_RESUME.name
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, R.raw.coin)
    }

    private fun playSound() {
        mediaPlayer.start()
    }

    companion object {
        const val NOTIFICATION_ID = 172
        const val NOTIFICATION_CHANNEL_ID = "473"
        const val NOTIFICATION_CHANNEL_NAME = "stopwatch_channel"

        private val _isTracking = MutableStateFlow(false)
        val isTracking: Flow<Boolean> = _isTracking

        private val _elapsedSeconds = MutableStateFlow(0L)
        val elapsedSeconds: Flow<Long> = _elapsedSeconds

        private val _elapsedMilliSeconds = MutableStateFlow(0L)
        val elapsedMilliSeconds: Flow<Long> = _elapsedMilliSeconds
    }

    enum class ServiceState {
        START_OR_RESUME,
        PAUSE,
        RESET
    }
}