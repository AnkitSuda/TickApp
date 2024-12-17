package com.ankitsuda.tickapp.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun TimerText(
    modifier: Modifier = Modifier,
    seconds: Long,
    onClick: () -> Unit,
) {
    val text = seconds.toString()
    val textParts = text.split("");

    BoxWithConstraints(
        modifier = modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            modifier = Modifier.fillMaxWidth(),
            targetState = Pair(textParts, text),
            contentAlignment = Alignment.Center,
            label = "TimerTextPart2",
            contentKey = {
                it.first.size
            },
            transitionSpec = {
                getContentTransform(
                    initial = initialState.first.size,
                    target = targetState.first.size,
                )
            },
        ) { pair ->
            val textMeasurer = rememberTextMeasurer()
            val mText = pair.second
            val fontSize by remember(mText, constraints) {
                derivedStateOf {

                    var textSize = 0.sp
                    var textWidth = 0
                    var textHeight = 0

                    while (textWidth < constraints.maxWidth && textHeight < constraints.maxHeight) {
                        val result = textMeasurer.measure(
                            text = mText,
                            style = TextStyle(
                                fontSize = (textSize.value + .5).sp,
                                platformStyle = PlatformTextStyle(
                                    includeFontPadding = false
                                )
                            )
                        )

                        textWidth = result.size.width
                        textHeight = result.size.height

                        if (textWidth <= constraints.maxWidth) {
                            textSize = result.layoutInput.style.fontSize
                        }
                    }

                    textSize
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                pair.first.forEach { text ->
                    TimerTextPart(
                        modifier = Modifier.fillMaxHeight(),
                        text = text,
                        fontSize = fontSize,
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerTextPart(
    modifier: Modifier = Modifier,
    text: String,
    fontSize: TextUnit,
) {

    AnimatedContent(
        modifier = modifier,
        targetState = text,
        contentAlignment = Alignment.Center,
        label = "TimerTextPart",
        transitionSpec = {
            getContentTransform(
                initial = 0,
                target = 1,
            )
        },
    ) { mText ->
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier,
                text = mText,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = fontSize,
                )
            )
        }
    }
}

private fun <T> AnimatedContentTransitionScope<T>.getContentTransform(
    initial: Int,
    target: Int,
): ContentTransform {
    return if (target > initial) {
        (slideInVertically(initialOffsetY = { height -> height }) + scaleIn() + fadeIn()).togetherWith(
            slideOutVertically(targetOffsetY = { height -> -height }) + scaleOut() + fadeOut()
        )
    } else {
        (slideInVertically(initialOffsetY = { height -> -height }) + scaleIn() + fadeIn()).togetherWith(
            slideOutVertically(targetOffsetY = { height -> height }) + scaleOut() + fadeOut()
        )
    }.using(SizeTransform(clip = false))
}