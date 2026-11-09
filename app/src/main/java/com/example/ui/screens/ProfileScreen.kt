package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.ui.components.DemoBanner
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.RiverBlue
import com.example.ui.theme.SaffronPrimary
import com.example.viewmodel.AppUiState

@Composable
fun ProfileScreen(
    state: AppUiState,
    onLanguageChange: (AppLanguage) -> Unit,
    onSubmitFeedback: (String, String, Int, String) -> Unit,
    onDeleteFeedback: (Long) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Pilgrimage Experience") }
    var ratingInput by remember { mutableIntStateOf(5) }
    var commentInput by remember { mutableStateOf("") }
    var feedbackSubmittedMessage by remember { mutableStateOf(false) }

    val feedbackCategories = listOf("Pilgrimage Experience", "3D & AR Feature", "Temple Information", "Pushkaralu 2027 Feedback", "Bug Report")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DemoBanner(customText = "USER PROFILE & PILGRIM PROGRESS • LOCAL ROOM DATABASE PERSISTENCE")
        }

        // Profile Avatar & Badges Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = SaffronPrimary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        text = "Godavari Heritage Explorer",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "Current Language: ${state.language.displayName} (${state.language.nativeName})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${state.completedTourIds.size}", fontWeight = FontWeight.Bold, color = SaffronPrimary, fontSize = 20.sp)
                            Text(text = "Tours Done", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${state.bookmarkedDestinationIds.size}", fontWeight = FontWeight.Bold, color = RiverBlue, fontSize = 20.sp)
                            Text(text = "Bookmarks", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${state.earnedBadges.size}", fontWeight = FontWeight.Bold, color = HeritageGold, fontSize = 20.sp)
                            Text(text = "Badges", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Earned Badges
        item {
            Text(
                text = "Earned Achievement Badges",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.earnedBadges.forEach { badge ->
                    Surface(
                        color = NavyDeep,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.MilitaryTech, contentDescription = null, tint = HeritageGold, modifier = Modifier.size(16.dp))
                            Text(text = badge, color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Language Preferences
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Preferred Language / భాష / भाषा",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            val isSelected = lang == state.language
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onLanguageChange(lang) },
                                color = if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lang.nativeName,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pilgrim Feedback Submission Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Pilgrim & Tourist Feedback",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Your Name / Pilgrim Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Rating Stars
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Rating:", style = MaterialTheme.typography.labelSmall)
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { ratingInput = star },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (star <= ratingInput) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star stars",
                                    tint = if (star <= ratingInput) HeritageGold else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        label = { Text("Feedback / Suggestions for Pushkaralu 2027") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Button(
                        onClick = {
                            if (commentInput.isNotBlank()) {
                                onSubmitFeedback(nameInput, categoryInput, ratingInput, commentInput)
                                commentInput = ""
                                nameInput = ""
                                feedbackSubmittedMessage = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submit Feedback to Local Database")
                    }

                    if (feedbackSubmittedMessage) {
                        Text(
                            text = "Thank you! Your feedback has been recorded into the persistent database.",
                            color = Color(0xFF16A34A),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        // Submitted Feedback History
        item {
            Text(
                text = "Your Recorded Feedback (${state.feedbackList.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(state.feedbackList) { fb ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = fb.pilgrimName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(text = "(${fb.rating} ★)", color = HeritageGold, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(text = fb.comment, style = MaterialTheme.typography.bodySmall)
                    }

                    IconButton(onClick = { onDeleteFeedback(fb.id) }, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
