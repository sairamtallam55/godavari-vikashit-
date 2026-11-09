package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HeritageRepository
import com.example.model.AppLanguage
import com.example.model.CulturalArticle
import com.example.ui.components.DemoBanner
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.RiverBlue
import com.example.ui.theme.SaffronPrimary

@Composable
fun CultureScreen(
    language: AppLanguage,
    bookmarkedIds: Set<String>,
    onToggleBookmark: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var viewingArticle by remember { mutableStateOf<CulturalArticle?>(null) }
    var isReadingAloud by remember { mutableStateOf(false) }

    val categories = listOf("All", "River Festivals", "Engineering Heritage", "Food Traditions", "Traditional Crafts", "Music & Performing Arts", "Architecture", "Sacred Rituals", "Folk Culture")
    val filteredArticles = HeritageRepository.culturalArticles.filter {
        selectedCategory == "All" || it.category == selectedCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DemoBanner(customText = "CULTURAL HERITAGE ARTICLES & TRADITIONS OF THE GODAVARI BASIN")

        // Categories Ribbon
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
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

        // Articles List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredArticles) { article ->
                val title = when (language) {
                    AppLanguage.TELUGU -> article.titleTe
                    AppLanguage.HINDI -> article.titleHi
                    AppLanguage.ENGLISH -> article.titleEn
                }
                val summary = when (language) {
                    AppLanguage.TELUGU -> article.summaryTe
                    AppLanguage.HINDI -> article.summaryHi
                    AppLanguage.ENGLISH -> article.summaryEn
                }
                val isBookmarked = bookmarkedIds.contains(article.id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { viewingArticle = article },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = SaffronPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = article.category,
                                    color = SaffronPrimary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${article.readTimeMinutes} min read",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(onClick = { onToggleBookmark(article.id) }) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = if (isBookmarked) HeritageGold else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Tradition tag chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            article.traditions.take(2).forEach { trad ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "• $trad",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Full Article Reader Dialog
    viewingArticle?.let { article ->
        val title = when (language) {
            AppLanguage.TELUGU -> article.titleTe
            AppLanguage.HINDI -> article.titleHi
            AppLanguage.ENGLISH -> article.titleEn
        }
        val content = when (language) {
            AppLanguage.TELUGU -> article.contentTe
            AppLanguage.HINDI -> article.contentHi
            AppLanguage.ENGLISH -> article.contentEn
        }

        AlertDialog(
            onDismissRequest = {
                viewingArticle = null
                isReadingAloud = false
            },
            title = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = SaffronPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = article.category,
                            color = SaffronPrimary,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Audio Read-Aloud Player Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { isReadingAloud = !isReadingAloud },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isReadingAloud) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                        contentDescription = "Read Aloud",
                                        tint = SaffronPrimary
                                    )
                                }
                                Text(
                                    text = if (isReadingAloud) "Narrating in ${language.displayName}..." else "Audio Narration Available",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Text(
                        text = "Core Traditions & Practices:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SaffronPrimary)
                    )
                    article.traditions.forEach { trad ->
                        Text(
                            text = "• $trad",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewingArticle = null
                    isReadingAloud = false
                }) {
                    Text("Close")
                }
            }
        )
    }
}
