package com.ankitsuda.tickapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ankitsuda.tickapp.R

@Composable
fun TimerControlsComp(
    modifier: Modifier = Modifier,
    canReset: Boolean,
    onResume: () -> Unit,
    onReset: () -> Unit
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        FloatingActionButton(
            modifier = Modifier,
            onClick = onResume,
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = stringResource(R.string.resume)
            )
        }
        if (canReset) {
            FloatingActionButton(
                modifier = Modifier,
                onClick = onReset,
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,

                    contentDescription = stringResource(R.string.reset)
                )
            }
        }
    }
}