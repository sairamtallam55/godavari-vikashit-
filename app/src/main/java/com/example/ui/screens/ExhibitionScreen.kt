package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.HeritageRepository
import com.example.model.AppLanguage
import com.example.model.ExhibitionItem
import com.example.ui.components.DemoBanner
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.RiverBlue
import com.example.ui.theme.SaffronPrimary

@Composable
fun ExhibitionScreen(language: AppLanguage) {
    var selectedItem by remember { mutableStateOf<ExhibitionItem?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Curated Archives, 1: Historical Timeline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DemoBanner(customText = "DIGITAL HERITAGE EXHIBITIONS & RETROSPECTIVE ARCHIVES")

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = SaffronPrimary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Archival Gallery (10)", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Historical Timeline", fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            // Archival Items List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(HeritageRepository.exhibitionItems) { item ->
                    val title = when (language) {
                        AppLanguage.TELUGU -> item.titleTe
                        AppLanguage.HINDI -> item.titleHi
                        AppLanguage.ENGLISH -> item.titleEn
                    }
                    val desc = when (language) {
                        AppLanguage.TELUGU -> item.descriptionTe
                        AppLanguage.HINDI -> item.descriptionHi
                        AppLanguage.ENGLISH -> item.descriptionEn
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedItem = item },
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
                                    color = RiverBlue.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = item.category,
                                        color = RiverBlue,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = item.yearEra,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronPrimary
                                    )
                                )
                            }

                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Archive: ${item.archivalNote}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Historical Timeline
            HistoricalTimelineView()
        }
    }

    // Detail Dialog
    selectedItem?.let { item ->
        val title = when (language) {
            AppLanguage.TELUGU -> item.titleTe
            AppLanguage.HINDI -> item.titleHi
            AppLanguage.ENGLISH -> item.titleEn
        }
        val desc = when (language) {
            AppLanguage.TELUGU -> item.descriptionTe
            AppLanguage.HINDI -> item.descriptionHi
            AppLanguage.ENGLISH -> item.descriptionEn
        }

        AlertDialog(
            onDismissRequest = { selectedItem = null },
            title = {
                Column {
                    Text(text = item.yearEra, style = MaterialTheme.typography.labelSmall.copy(color = SaffronPrimary, fontWeight = FontWeight.Bold))
                    Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = desc, style = MaterialTheme.typography.bodyMedium)
                    Divider()
                    Text(
                        text = "Curatorial Preservation Record:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SaffronPrimary)
                    )
                    Text(
                        text = item.archivalNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedItem = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun HistoricalTimelineView() {
    val timelineEvents = listOf(
        Pair("Ancient / Treta Yuga", "Sage Gautama brings River Godavari to Earth; Lord Rama stays in Panchavati along Godavari."),
        Pair("1st Century BCE", "Satavahana Empire rules the Godavari valley from Kotilingala and Paithan."),
        Pair("9th - 11th Century CE", "Eastern Chalukyas construct monolithic shrines at Draksharamam, Bheemavaram, and Palakollu."),
        Pair("1022 CE", "King Rajaraja Narendra commissions Adikavi Nannaya in Rajahmundry to begin the Andhra Mahabharatam."),
        Pair("17th Century CE", "Bhakta Ramadasu (Kancharla Gopanna) constructs the Bhadrachalam Sri Sitaramachandra Swamy temple."),
        Pair("1852 CE", "Sir Arthur Cotton completes the 3.6 km Dowleswaram Anicut, transforming delta agriculture."),
        Pair("1900 CE", "Havelock Bridge over Godavari opens, connecting Madras to Howrah via 56 stone masonry piers."),
        Pair("1956 CE", "First major post-independence Godavari Pushkaralu brings over 2 million pilgrims to Rajahmundry."),
        Pair("2015 CE", "Maha Pushkaram cycle celebrated with monumental ghat lighting and riverfront upgrades."),
        Pair("2027 CE", "Upcoming Godavari Pushkaralu — Godavari Virasat digital heritage exploration platform ready.")
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(timelineEvents) { event ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(28.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(14.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = SaffronPrimary
                    ) {}
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(65.dp)
                            .background(HeritageGold.copy(alpha = 0.5f))
                    )
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = event.first,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SaffronPrimary
                            )
                        )
                        Text(
                            text = event.second,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
