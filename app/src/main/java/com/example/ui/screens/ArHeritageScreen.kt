package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.HeritageRepository
import com.example.model.AppLanguage
import com.example.model.Model3DItem
import com.example.ui.components.CameraPreviewView
import com.example.ui.components.DemoBanner
import com.example.ui.components.Interactive3DCanvas
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArHeritageScreen(
    language: AppLanguage,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionRequested by remember { mutableStateOf(false) }
    var isArActive by remember { mutableStateOf(false) }
    var force3DFallback by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(HeritageRepository.model3DItems.first()) }
    var showModelDropdown by remember { mutableStateOf(false) }
    var showInfoPanel by remember { mutableStateOf(true) }

    // AR Object Transform state
    var objectScale by remember { mutableFloatStateOf(1.0f) }
    var objectRotY by remember { mutableFloatStateOf(0f) }
    var objectOffsetX by remember { mutableFloatStateOf(0f) }
    var objectOffsetY by remember { mutableFloatStateOf(0f) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionRequested = true
        hasCameraPermission = isGranted
        if (isGranted) {
            isArActive = true
            force3DFallback = false
        } else {
            force3DFallback = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isArActive && !force3DFallback) "LIVE AR CAMERA MODE" else "3D HERITAGE VIEWER (FALLBACK)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isArActive && !force3DFallback) SaffronPrimary else HeritageGold
                            )
                        )
                        Text(
                            text = "Godavari Virasat AR Engine",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f))
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Switch between Real Camera AR and 3D viewer fallback
                    IconButton(
                        onClick = {
                            if (isArActive && !force3DFallback) {
                                force3DFallback = true
                            } else {
                                if (hasCameraPermission) {
                                    force3DFallback = false
                                    isArActive = true
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isArActive && !force3DFallback) Icons.Default.Videocam else Icons.Default.ViewInAr,
                            contentDescription = "Toggle AR/3D",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDeep)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(NavyDeep)
        ) {
            if (isArActive && hasCameraPermission && !force3DFallback) {
                // REAL CAMERA AR EXPERIENCE
                // Layer 1: Live Back Camera Feed
                CameraPreviewView(
                    modifier = Modifier.fillMaxSize()
                )

                // Layer 2: 3D Heritage Artifact overlay in camera environment
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, gestureZoom, gestureRotation ->
                                objectOffsetX += pan.x
                                objectOffsetY += pan.y
                                objectScale = (objectScale * gestureZoom).coerceIn(0.4f, 2.8f)
                                objectRotY += gestureRotation
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = objectOffsetX.dp, y = objectOffsetY.dp)
                            .size((280 * objectScale).dp)
                    ) {
                        Interactive3DCanvas(
                            modelType = selectedModel.modelType,
                            modifier = Modifier.fillMaxSize(),
                            isDarkTheme = true,
                            accentColor = HeritageGold,
                            showControls = false
                        )
                    }
                }

                // Layer 3: AR HUD Overlays (Top info badge, Reticle, Instructions, Object Picker)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top AR Mode Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xCC000000),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                )
                                Text(
                                    text = "LIVE AR CAMERA • REAL-TIME FEED",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Exit AR Button
                        Button(
                            onClick = {
                                isArActive = false
                                force3DFallback = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.85f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exit AR", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Bottom Controls & Object Details Card
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Gesture Instruction Pill
                        Surface(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = Color(0xCC0B192C),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Drag to reposition • Pinch to scale object in room",
                                color = HeritageGoldLight,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // Object Selector & Info Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NavyDeep.copy(alpha = 0.92f)),
                            shape = RoundedCornerShape(16.dp)
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
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = selectedModel.nameEn,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = "${selectedModel.era} • ${selectedModel.discoveryLocation}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = HeritageGold)
                                        )
                                    }

                                    // Switch Model Dropdown Button
                                    OutlinedButton(
                                        onClick = { showModelDropdown = true },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) {
                                        Text("Change Artifact")
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                    }

                                    DropdownMenu(
                                        expanded = showModelDropdown,
                                        onDismissRequest = { showModelDropdown = false }
                                    ) {
                                        HeritageRepository.model3DItems.forEach { model ->
                                            DropdownMenuItem(
                                                text = { Text(model.nameEn) },
                                                onClick = {
                                                    selectedModel = model
                                                    showModelDropdown = false
                                                    objectOffsetX = 0f
                                                    objectOffsetY = 0f
                                                    objectScale = 1.0f
                                                }
                                            )
                                        }
                                    }
                                }

                                if (showInfoPanel) {
                                    Text(
                                        text = selectedModel.descriptionEn,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
                                    )
                                    Text(
                                        text = "Architectural Note: ${selectedModel.architecturalSignificance}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = SaffronLight)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // 3D FALLBACK OR INITIAL ONBOARDING VIEW
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    DemoBanner(customText = "CLEAR DISTINCTION: THIS IS THE 3D VIEWER (NOT LIVE AR)")

                    // Status and Start AR Call to Action
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavyLight),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = SaffronPrimary
                                )
                                Text(
                                    text = "Start Live AR Experience",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }

                            Text(
                                text = "Place sacred Godavari heritage artifacts directly onto real-world surfaces through your device camera with interactive rotation and scaling.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
                            )

                            if (permissionRequested && !hasCameraPermission) {
                                Surface(
                                    color = Color.Red.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Camera permission was denied. Interactive 3D viewer is active below.",
                                        color = Color(0xFFFCA5A5),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (hasCameraPermission) {
                                        isArActive = true
                                        force3DFallback = false
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.ViewInAr, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Launch Camera AR Mode", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Interactive 3D Fallback Canvas
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(containerColor = NavySurface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = selectedModel.nameEn,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = "${selectedModel.era} • Interactive 3D Model",
                                        style = MaterialTheme.typography.labelSmall.copy(color = HeritageGold)
                                    )
                                }

                                TextButton(onClick = { showModelDropdown = true }) {
                                    Text("Switch Model", color = HeritageGold)
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = HeritageGold)
                                }

                                DropdownMenu(
                                    expanded = showModelDropdown,
                                    onDismissRequest = { showModelDropdown = false }
                                ) {
                                    HeritageRepository.model3DItems.forEach { model ->
                                        DropdownMenuItem(
                                            text = { Text(model.nameEn) },
                                            onClick = {
                                                selectedModel = model
                                                showModelDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Interactive3DCanvas(
                                modelType = selectedModel.modelType,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                isDarkTheme = true,
                                accentColor = HeritageGold,
                                showControls = true
                            )

                            Text(
                                text = selectedModel.descriptionEn,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
                            )
                        }
                    }
                }
            }
        }
    }
}
