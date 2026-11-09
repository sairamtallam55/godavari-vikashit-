package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HeritageRepository
import com.example.model.AppLanguage
import com.example.model.Model3DItem
import com.example.ui.components.DemoBanner
import com.example.ui.components.Interactive3DCanvas
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavySurface
import com.example.ui.theme.SaffronPrimary

@Composable
fun Gallery3DScreen(
    language: AppLanguage,
    selectedModel: Model3DItem,
    onSelectModel: (Model3DItem) -> Unit,
    onLaunchAr: () -> Unit
) {
    val scrollState = rememberScrollState()

    val name = when (language) {
        AppLanguage.TELUGU -> selectedModel.nameTe
        AppLanguage.HINDI -> selectedModel.nameHi
        AppLanguage.ENGLISH -> selectedModel.nameEn
    }
    val desc = when (language) {
        AppLanguage.TELUGU -> selectedModel.descriptionTe
        AppLanguage.HINDI -> selectedModel.descriptionHi
        AppLanguage.ENGLISH -> selectedModel.descriptionEn
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        DemoBanner(customText = "3D DEMONSTRATION GALLERY — ILLUSTRATIVE HISTORICAL RECONSTRUCTIONS")

        // 3D Model Selector Horizontal Ribbon
        Text(
            text = when (language) {
                AppLanguage.TELUGU -> "పురాతన 3డి నమూనాలు (8)"
                AppLanguage.HINDI -> "प्राचीन 3डी मॉडल संग्रह (8)"
                AppLanguage.ENGLISH -> "Heritage 3D Artifacts (8)"
            },
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(HeritageRepository.model3DItems) { model ->
                val isSelected = model.id == selectedModel.id
                val mName = when (language) {
                    AppLanguage.TELUGU -> model.nameTe
                    AppLanguage.HINDI -> model.nameHi
                    AppLanguage.ENGLISH -> model.nameEn
                }
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSelectModel(model) },
                    color = if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RotateRight,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = mName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        // Main Interactive 3D Canvas Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Interactive3DCanvas(
                    modelType = selectedModel.modelType,
                    modifier = Modifier.fillMaxSize(),
                    isDarkTheme = true,
                    accentColor = HeritageGold,
                    showControls = true
                )

                // Top Left Overlay Tag
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = selectedModel.category,
                        color = HeritageGold,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Action: Launch AR
        Button(
            onClick = onLaunchAr,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.ViewInAr, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Project This Model in Live Camera AR",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        // Model Information & Archaeological Context
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Historical Era",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = selectedModel.era,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Origin / Location",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = selectedModel.discoveryLocation,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Architectural & Engineering Significance:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SaffronPrimary)
                        )
                        Text(
                            text = selectedModel.architecturalSignificance,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
