package com.ankitsuda.tickapp.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ankitsuda.tickapp.service.StopwatchService
import com.ankitsuda.tickapp.ui.components.TimerControlsComp
import com.ankitsuda.tickapp.ui.components.TimerText

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val isTracking by StopwatchService.isTracking.collectAsState(false)
    val elapsedSeconds by StopwatchService.elapsedSeconds.collectAsState( 0)
    val context = LocalContext.current

    fun toggle() {
        commandService(
            context,
            if (isTracking) StopwatchService.ServiceState.PAUSE else StopwatchService.ServiceState.START_OR_RESUME
        )
    }

    fun reset() {
        commandService(context, StopwatchService.ServiceState.RESET)
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            TimerText(
                modifier = Modifier
                    .fillMaxSize(),
                seconds = elapsedSeconds,
                onClick = ::toggle,
            )
            if (!isTracking) {
                TimerControlsComp(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    canReset = elapsedSeconds > 0,
                    onResume = ::toggle,
                    onReset = ::reset,
                )
            }
        }
    }
}


private fun commandService(context: Context, serviceState: StopwatchService.ServiceState) {
    val intent = Intent(context, StopwatchService::class.java)
    intent.action = serviceState.name

    context.startService(intent)
}