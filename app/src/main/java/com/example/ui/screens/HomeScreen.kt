package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AppLanguage
import com.example.model.HeritageDestination
import com.example.ui.components.AppTab
import com.example.ui.components.DemoBanner
import com.example.ui.components.UiStrings
import com.example.ui.theme.*
import com.example.viewmodel.AppUiState

@Composable
fun HomeScreen(
    state: AppUiState,
    onTabSelected: (AppTab) -> Unit,
    onSelectDestination: (HeritageDestination) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onCategoryFilterChange: (String) -> Unit
) {
    val lang = state.language
    val filteredDestinations = state.managedDestinations.filter { dest ->
        (state.destinationCategoryFilter == "All" || dest.category == state.destinationCategoryFilter) &&
                (state.searchQuery.isBlank() ||
                        dest.nameEn.contains(state.searchQuery, ignoreCase = true) ||
                        dest.nameTe.contains(state.searchQuery, ignoreCase = true) ||
                        dest.descriptionEn.contains(state.searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Section with Generated Pushkaralu River Ghat Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                // Generated Hero Image
                Image(
                    painter = painterResource(id = R.drawable.img_hero_godavari_1794151276283),
                    contentDescription = "Godavari Pushkar Ghat Sunset",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dramatic Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    NavyDeep.copy(alpha = 0.6f),
                                    NavyDeep
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = SaffronPrimary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "GODAVARI PUSHKARALU 2027",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = UiStrings.appTitle(lang),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )

                    Text(
                        text = UiStrings.tagline(lang),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = HeritageGoldLight
                        )
                    )
                }
            }
        }

        // Prominent Demo Content Notification Banner
        item {
            DemoBanner(
                modifier = Modifier.padding(horizontal = 16.dp),
                customText = "DEMONSTRATION CONTENT — NOT OFFICIAL OR VERIFIED • HACKATHON INNOVATION PLATFORM"
            )
        }

        // Quick Launch Feature Action Cards (AR, 3D, Tours, AI, Quiz, Map)
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = when (lang) {
                        AppLanguage.TELUGU -> "ప్రత్యేక అనుభవాలు"
                        AppLanguage.HINDI -> "प्रमुख अनुभव"
                        AppLanguage.ENGLISH -> "Featured Exploration Portals"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = when (lang) {
                            AppLanguage.TELUGU -> "ఏఆర్ అనుభవం"
                            AppLanguage.HINDI -> "एआर अनुभव"
                            AppLanguage.ENGLISH -> "AR Live View"
                        },
                        subtitle = "Camera Overlay",
                        icon = Icons.Default.ViewInAr,
                        containerColor = SaffronPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(AppTab.AR_EXPERIENCE) }
                    )

                    QuickActionCard(
                        title = when (lang) {
                            AppLanguage.TELUGU -> "3డి గ్యాలరీ"
                            AppLanguage.HINDI -> "3डी दीर्घा"
                            AppLanguage.ENGLISH -> "3D Gallery"
                        },
                        subtitle = "8 Heritage Models",
                        icon = Icons.Default.RotateRight,
                        containerColor = RiverBlue,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(AppTab.GALLERY_3D) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = when (lang) {
                            AppLanguage.TELUGU -> "వర్చువల్ యాత్ర"
                            AppLanguage.HINDI -> "वर्चुअल यात्रा"
                            AppLanguage.ENGLISH -> "Virtual Tours"
                        },
                        subtitle = "5 Sacred Circuits",
                        icon = Icons.Default.Tour,
                        containerColor = SacredTeal,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(AppTab.TOURS_MAP) }
                    )

                    QuickActionCard(
                        title = when (lang) {
                            AppLanguage.TELUGU -> "ఏఐ గైడ్"
                            AppLanguage.HINDI -> "एआई गाइड"
                            AppLanguage.ENGLISH -> "AI Guide"
                        },
                        subtitle = "Multilingual Chat",
                        icon = Icons.Default.Psychology,
                        containerColor = NavyLight,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(AppTab.AI_GUIDE) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = when (lang) {
                            AppLanguage.TELUGU -> "క్విజ్ & నేర్చుకోండి"
                            AppLanguage.HINDI -> "ज्ञान प्रश्नोत्तरी"
                            AppLanguage.ENGLISH -> "Heritage Quiz"
                        },
                        subtitle = "15 Questions",
                        icon = Icons.Default.Quiz,
                        containerColor = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(AppTab.QUIZ) }
                    )

                    QuickActionCard(
                        title = when (lang) {
                            AppLanguage.TELUGU -> "డిజిటల్ ప్రదర్శన"
                            AppLanguage.HINDI -> "डिजिटल प्रदर्शनी"
                            AppLanguage.ENGLISH -> "Exhibition"
                        },
                        subtitle = "10 Historic Archives",
                        icon = Icons.Default.Collections,
                        containerColor = Color(0xFF9D174D),
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(AppTab.EXHIBITION) }
                    )
                }
            }
        }

        // Category Filter Chips
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when (lang) {
                        AppLanguage.TELUGU -> "విభాగాలు"
                        AppLanguage.HINDI -> "श्रेणियां"
                        AppLanguage.ENGLISH -> "Explore by Category"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                val categories = listOf("All", "Sacred Ghat", "Ancient Temple", "Colonial Engineering", "River Valley", "Craft Village")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = cat == state.destinationCategoryFilter
                        FilterChip(
                            selected = isSelected,
                            onClick = { onCategoryFilterChange(cat) },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaffronPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Heritage Destinations List
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (lang) {
                            AppLanguage.TELUGU -> "గోదావరి వారసత్వ ప్రదేశాలు (${filteredDestinations.size})"
                            AppLanguage.HINDI -> "गोदावरी विरासत स्थल (${filteredDestinations.size})"
                            AppLanguage.ENGLISH -> "Heritage Destinations (${filteredDestinations.size})"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                filteredDestinations.forEach { destination ->
                    HeritageDestinationCard(
                        destination = destination,
                        language = lang,
                        isBookmarked = state.bookmarkedDestinationIds.contains(destination.id),
                        onToggleBookmark = { onToggleBookmark(destination.id) },
                        onClick = { onSelectDestination(destination) }
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun HeritageDestinationCard(
    destination: HeritageDestination,
    language: AppLanguage,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onClick: () -> Unit
) {
    val name = when (language) {
        AppLanguage.TELUGU -> destination.nameTe
        AppLanguage.HINDI -> destination.nameHi
        AppLanguage.ENGLISH -> destination.nameEn
    }
    val desc = when (language) {
        AppLanguage.TELUGU -> destination.descriptionTe
        AppLanguage.HINDI -> destination.descriptionHi
        AppLanguage.ENGLISH -> destination.descriptionEn
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Optional image header if available
            if (destination.drawableResName == "img_temple_heritage") {
                Image(
                    painter = painterResource(id = R.drawable.img_temple_heritage_1794151291126),
                    contentDescription = name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )
            } else if (destination.drawableResName == "img_hero_godavari") {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_godavari_1794151276283),
                    contentDescription = name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = SaffronPrimary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = destination.category,
                                    color = SaffronPrimary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Surface(
                                color = if (destination.verificationStatus == "VERIFIED") Color(0xFF16A34A).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = destination.verificationStatus,
                                    color = if (destination.verificationStatus == "VERIFIED") Color(0xFF16A34A) else Color(0xFFD97706),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${destination.district} • ${destination.era}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) HeritageGold else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Key sights chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    destination.highlights.take(2).forEach { hl ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "• $hl",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
