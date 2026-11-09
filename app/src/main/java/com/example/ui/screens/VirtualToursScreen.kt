package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HeritageRepository
import com.example.model.AppLanguage
import com.example.model.VirtualTour
import com.example.ui.components.DemoBanner
import com.example.ui.theme.*
import com.example.viewmodel.AppUiState

@Composable
fun VirtualToursScreen(
    state: AppUiState,
    onStartTour: (VirtualTour) -> Unit,
    onNextStop: () -> Unit,
    onPrevStop: () -> Unit,
    onCloseTour: () -> Unit
) {
    val lang = state.language
    val activeTour = state.selectedTour

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        DemoBanner(customText = "VIRTUAL HERITAGE TOURS — SIMULATED DEMO CIRCUITS FOR PUSHKARALU")

        if (activeTour != null) {
            // ACTIVE TOUR PLAYER
            ActiveTourPlayer(
                tour = activeTour,
                currentStopIdx = state.currentTourStopIndex,
                lang = lang,
                onNext = onNextStop,
                onPrev = onPrevStop,
                onClose = onCloseTour
            )
        } else {
            // TOURS LIST
            Text(
                text = when (lang) {
                    AppLanguage.TELUGU -> "గోదావరి వర్చువల్ యాత్రలు (5)"
                    AppLanguage.HINDI -> "गोदावरी वर्चुअल यात्राएं (5)"
                    AppLanguage.ENGLISH -> "Virtual Heritage Circuits (5)"
                },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(HeritageRepository.virtualTours) { tour ->
                    val isCompleted = state.completedTourIds.contains(tour.id)
                    val title = when (lang) {
                        AppLanguage.TELUGU -> tour.titleTe
                        AppLanguage.HINDI -> tour.titleHi
                        AppLanguage.ENGLISH -> tour.titleEn
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onStartTour(tour) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = RiverBlue.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = tour.region,
                                        color = RiverBlue,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                if (isCompleted) {
                                    Surface(
                                        color = Color(0xFF16A34A).copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF16A34A),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Completed",
                                                color = Color(0xFF16A34A),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "${tour.estimatedMinutes} Mins", style = MaterialTheme.typography.labelSmall)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = "${tour.stops.size} Stops", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Button(
                                onClick = { onStartTour(tour) },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isCompleted) "Replay Tour" else "Start Virtual Tour")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveTourPlayer(
    tour: VirtualTour,
    currentStopIdx: Int,
    lang: AppLanguage,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onClose: () -> Unit
) {
    val stop = tour.stops[currentStopIdx]
    var panOffset by remember { mutableFloatStateOf(0f) }
    var isAudioPlaying by remember { mutableStateOf(false) }

    val stopTitle = when (lang) {
        AppLanguage.TELUGU -> stop.titleTe
        AppLanguage.HINDI -> stop.titleHi
        AppLanguage.ENGLISH -> stop.titleEn
    }
    val stopDesc = when (lang) {
        AppLanguage.TELUGU -> stop.descriptionTe
        AppLanguage.HINDI -> stop.descriptionHi
        AppLanguage.ENGLISH -> stop.descriptionEn
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Player Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when (lang) {
                        AppLanguage.TELUGU -> tour.titleTe
                        AppLanguage.HINDI -> tour.titleHi
                        AppLanguage.ENGLISH -> tour.titleEn
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Stop ${currentStopIdx + 1} of ${tour.stops.size}",
                    style = MaterialTheme.typography.labelSmall.copy(color = SaffronPrimary, fontWeight = FontWeight.Bold)
                )
            }

            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Tour")
            }
        }

        // Progress Indicator Bar
        LinearProgressIndicator(
            progress = { (currentStopIdx + 1).toFloat() / tour.stops.size.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = SaffronPrimary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // 360° Panorama Simulation Canvas / Viewport
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            panOffset = (panOffset + dragAmount.x * 0.5f).coerceIn(-180f, 180f)
                        }
                    }
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                NavyDeep,
                                RiverBlue.copy(alpha = 0.5f),
                                HeritageGold.copy(alpha = 0.3f),
                                RiverBlue.copy(alpha = 0.5f),
                                NavyDeep
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PanoramaHorizontal,
                        contentDescription = null,
                        tint = HeritageGold,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "360° Simulated Panorama View",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Pan angle: ${panOffset.toInt()}° • Drag sideways to explore sight",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.75f))
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stop.keySight,
                        color = HeritageGoldLight,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Stop Content Details Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stopTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Simulated Audio Narration Guide
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { isAudioPlaying = !isAudioPlaying },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAudioPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                    contentDescription = "Audio Guide",
                                    tint = SaffronPrimary
                                )
                            }
                            Text(
                                text = if (isAudioPlaying) "Playing Audio Narration..." else "Audio Guide Available",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = "01:45 min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = stopDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    color = SaffronPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = SaffronPrimary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Pilgrim Guideline: ${stop.ritualGuideline}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SaffronPrimary
                        )
                    }
                }
            }
        }

        // Bottom Navigation Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onPrev,
                enabled = currentStopIdx > 0,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Previous")
            }

            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
            ) {
                Text(if (currentStopIdx == tour.stops.size - 1) "Complete Tour" else "Next Stop")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}
