package com.chris.chipherlink.ui.call

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chris.chipherlink.ui.theme.CipherLinkTeal

@Composable
fun CallScreen(
    chatId: String,
    contactName: String,
    onNavigateBack: () -> Unit,
    viewModel: CallViewModel = viewModel()
) {
    val callInfo by viewModel.callInfo.collectAsState()

    LaunchedEffect(chatId, contactName) {
        viewModel.initiateCall(chatId, contactName)
    }

    LaunchedEffect(callInfo.state) {
        if (callInfo.state == CallState.ENDED) {
            onNavigateBack()
        }
    }

    val backgroundColor by animateColorAsState(
        targetValue = when (callInfo.state) {
            CallState.IN_CALL -> MaterialTheme.colorScheme.surface
            CallState.CALLING, CallState.CONNECTING -> MaterialTheme.colorScheme.primaryContainer
            CallState.ENDING -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.background
        },
        animationSpec = tween(500)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section - contact info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 60.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contactName.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = CipherLinkTeal
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = contactName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val statusText = when (callInfo.state) {
                    CallState.CALLING -> "Calling..."
                    CallState.RINGING -> "Ringing..."
                    CallState.CONNECTING -> "Connecting..."
                    CallState.IN_CALL -> viewModel.formatDuration(callInfo.duration)
                    CallState.ENDING -> "Ending call..."
                    CallState.ENDED -> "Call ended"
                    CallState.FAILED -> "Call failed"
                    CallState.IDLE -> ""
                }

                val statusColor = when (callInfo.state) {
                    CallState.IN_CALL -> MaterialTheme.colorScheme.primary
                    CallState.FAILED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = statusColor
                )
            }

            // Middle section - call quality indicator (beta)
            if (callInfo.state == CallState.IN_CALL) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "BETA",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Audio quality may vary",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            // Bottom section - call controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                if (callInfo.state == CallState.IN_CALL) {
                    // Control buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Mute button
                        IconButton(
                            onClick = viewModel::toggleMute,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (callInfo.isMuted)
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        ) {
                            Icon(
                                imageVector = if (callInfo.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = if (callInfo.isMuted) "Unmute" else "Mute",
                                modifier = Modifier.size(28.dp),
                                tint = if (callInfo.isMuted)
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Speaker button
                        IconButton(
                            onClick = viewModel::toggleSpeaker,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (callInfo.isSpeakerOn)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        ) {
                            Icon(
                                imageVector = if (callInfo.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = if (callInfo.isSpeakerOn) "Speaker off" else "Speaker on",
                                modifier = Modifier.size(28.dp),
                                tint = if (callInfo.isSpeakerOn)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // End call button
                Button(
                    onClick = viewModel::endCall,
                    modifier = Modifier
                        .size(72.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}
