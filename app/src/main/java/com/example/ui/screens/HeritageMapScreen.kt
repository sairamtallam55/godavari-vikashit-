package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HeritageRepository
import com.example.model.AppLanguage
import com.example.model.HeritageDestination
import com.example.ui.components.DemoBanner
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.RiverBlue
import com.example.ui.theme.SaffronPrimary

@Composable
fun HeritageMapScreen(
    language: AppLanguage,
    onSelectDestination: (HeritageDestination) -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedDestination by remember { mutableStateOf<HeritageDestination?>(HeritageRepository.destinations.first()) }
    var userLocationGranted by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        userLocationGranted = granted
    }

    var mapZoom by remember { mutableFloatStateOf(1.0f) }
    var mapOffsetX by remember { mutableFloatStateOf(0f) }
    var mapOffsetY by remember { mutableFloatStateOf(0f) }

    val categories = listOf("All", "Sacred Ghat", "Ancient Temple", "Colonial Engineering", "River Valley", "Craft Village")
    val filteredDestinations = HeritageRepository.destinations.filter {
        selectedCategory == "All" || it.category == selectedCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DemoBanner(customText = "INTERACTIVE HERITAGE MAP — COORDINATES & CIRUCITS OF THE GODAVARI BASIN")

        // Top Filter Bar
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SaffronPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Map Canvas Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = NavyDeep),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            mapOffsetX += pan.x
                            mapOffsetY += pan.y
                            mapZoom = (mapZoom * zoom).coerceIn(0.8f, 2.5f)
                        }
                    }
            ) {
                // Vector Canvas Map of River Godavari
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(filteredDestinations) {
                            detectTapGestures { tapOffset ->
                                // Tap detection for markers
                                val canvasW = size.width.toFloat()
                                val canvasH = size.height.toFloat()
                                filteredDestinations.forEach { dest ->
                                    val markerPos = computeMapPos(dest.latitude, dest.longitude, canvasW, canvasH, mapZoom, mapOffsetX, mapOffsetY)
                                    val dist = (tapOffset - markerPos).getDistance()
                                    if (dist < 35f) {
                                        selectedDestination = dest
                                    }
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // Draw Godavari River Course Ribbon
                    val riverPath = Path()
                    val p0 = computeMapPos(19.99, 73.78, w, h, mapZoom, mapOffsetX, mapOffsetY) // Trimbakeshwar
                    val p1 = computeMapPos(18.87, 77.95, w, h, mapZoom, mapOffsetX, mapOffsetY) // Basar
                    val p2 = computeMapPos(17.66, 80.88, w, h, mapZoom, mapOffsetX, mapOffsetY) // Bhadrachalam
                    val p3 = computeMapPos(17.51, 81.33, w, h, mapZoom, mapOffsetX, mapOffsetY) // Papikondalu
                    val p4 = computeMapPos(17.15, 81.60, w, h, mapZoom, mapOffsetX, mapOffsetY) // Pattiseema
                    val p5 = computeMapPos(17.00, 81.77, w, h, mapZoom, mapOffsetX, mapOffsetY) // Rajahmundry
                    val p6 = computeMapPos(16.94, 81.76, w, h, mapZoom, mapOffsetX, mapOffsetY) // Dowleswaram Delta
                    val p7 = computeMapPos(16.45, 81.89, w, h, mapZoom, mapOffsetX, mapOffsetY) // Dindi Backwaters
                    val p8 = computeMapPos(16.33, 81.72, w, h, mapZoom, mapOffsetX, mapOffsetY) // Antarvedi Confluence

                    riverPath.moveTo(p0.x, p0.y)
                    riverPath.cubicTo(p0.x + 40, p0.y + 10, p1.x - 30, p1.y - 20, p1.x, p1.y)
                    riverPath.cubicTo(p1.x + 30, p1.y + 20, p2.x - 40, p2.y - 10, p2.x, p2.y)
                    riverPath.cubicTo(p2.x + 20, p2.y + 15, p3.x - 20, p3.y - 15, p3.x, p3.y)
                    riverPath.cubicTo(p3.x + 15, p3.y + 15, p4.x - 15, p4.y - 10, p4.x, p4.y)
                    riverPath.cubicTo(p4.x + 10, p4.y + 10, p5.x - 10, p5.y - 10, p5.x, p5.y)
                    riverPath.cubicTo(p5.x + 10, p5.y + 10, p6.x - 10, p6.y - 10, p6.x, p6.y)
                    riverPath.cubicTo(p6.x + 10, p6.y + 10, p7.x - 10, p7.y - 10, p7.x, p7.y)
                    riverPath.lineTo(p8.x, p8.y)

                    // Draw river glow & flow stroke
                    drawPath(
                        path = riverPath,
                        color = RiverBlue.copy(alpha = 0.35f),
                        style = Stroke(width = 22f * mapZoom)
                    )
                    drawPath(
                        path = riverPath,
                        color = RiverBlue,
                        style = Stroke(width = 10f * mapZoom)
                    )

                    // Draw Destination Markers
                    filteredDestinations.forEach { dest ->
                        val pos = computeMapPos(dest.latitude, dest.longitude, w, h, mapZoom, mapOffsetX, mapOffsetY)
                        val isSelected = dest.id == selectedDestination?.id

                        // Outer beacon pulse
                        if (isSelected) {
                            drawCircle(
                                color = SaffronPrimary.copy(alpha = 0.35f),
                                radius = 22f * mapZoom,
                                center = pos
                            )
                        }

                        // Marker dot
                        drawCircle(
                            color = if (isSelected) SaffronPrimary else HeritageGold,
                            radius = if (isSelected) 10f * mapZoom else 7f * mapZoom,
                            center = pos
                        )
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 4f * mapZoom else 2.5f * mapZoom,
                            center = pos
                        )
                    }
                }

                // Map HUD Controls (Zoom In/Out, Reset, Location)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { mapZoom = (mapZoom * 1.25f).coerceAtMost(2.5f) },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = NavyDeep.copy(alpha = 0.85f)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White)
                    }
                    IconButton(
                        onClick = { mapZoom = (mapZoom / 1.25f).coerceAtLeast(0.8f) },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = NavyDeep.copy(alpha = 0.85f)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                    }
                    IconButton(
                        onClick = {
                            if (!userLocationGranted) {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (userLocationGranted) SaffronPrimary else NavyDeep.copy(alpha = 0.85f)
                        ),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "My Location",
                            tint = Color.White
                        )
                    }
                }

                // Map Legend Overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RiverBlue))
                        Text("Godavari River", color = Color.White, style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(HeritageGold))
                        Text("Heritage Site", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Selected Destination Inspection Card
        selectedDestination?.let { dest ->
            val name = when (language) {
                AppLanguage.TELUGU -> dest.nameTe
                AppLanguage.HINDI -> dest.nameHi
                AppLanguage.ENGLISH -> dest.nameEn
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "${dest.category} • ${dest.district}",
                                style = MaterialTheme.typography.labelSmall.copy(color = SaffronPrimary)
                            )
                        }

                        Surface(
                            color = if (dest.verificationStatus == "VERIFIED") Color(0xFF16A34A).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = dest.verificationStatus,
                                color = if (dest.verificationStatus == "VERIFIED") Color(0xFF16A34A) else Color(0xFFD97706),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Coordinates: ${String.format("%.4f", dest.latitude)}° N, ${String.format("%.4f", dest.longitude)}° E",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val gmmIntentUri = Uri.parse("geo:${dest.latitude},${dest.longitude}?q=${dest.latitude},${dest.longitude}(${Uri.encode(dest.nameEn)})")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                try {
                                    context.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    // Fallback to browser Google Maps
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${dest.latitude},${dest.longitude}"))
                                    context.startActivity(webIntent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RiverBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Get Directions", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = { onSelectDestination(dest) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("View Details", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

// Convert geographic lat/long into normalized canvas coordinates
private fun computeMapPos(
    lat: Double,
    lon: Double,
    width: Float,
    height: Float,
    zoom: Float,
    panX: Float,
    panY: Float
): Offset {
    val minLat = 16.0
    val maxLat = 20.2
    val minLon = 73.5
    val maxLon = 82.6

    val normX = ((lon - minLon) / (maxLon - minLon)).toFloat().coerceIn(0f, 1f)
    val normY = (1.0f - ((lat - minLat) / (maxLat - minLat)).toFloat()).coerceIn(0f, 1f)

    val mapCenterX = width / 2f
    val mapCenterY = height / 2f

    val rawX = normX * width
    val rawY = normY * height

    val finalX = mapCenterX + (rawX - mapCenterX) * zoom + panX
    val finalY = mapCenterY + (rawY - mapCenterY) * zoom + panY

    return Offset(finalX, finalY)
}
