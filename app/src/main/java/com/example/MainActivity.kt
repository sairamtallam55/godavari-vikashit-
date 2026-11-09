package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AppLanguage
import com.example.ui.components.AppTab
import com.example.ui.components.BottomNavBar
import com.example.ui.components.TopHeritageBar
import com.example.ui.screens.*
import com.example.ui.theme.GodavariVirasatTheme
import com.example.ui.theme.SaffronPrimary
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GodavariVirasatTheme {
                GodavariVirasatApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GodavariVirasatApp(
    viewModel: MainViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSearchDialog by remember { mutableStateOf(false) }
    var toursMapSubTab by remember { mutableIntStateOf(0) } // 0: Tours, 1: Map

    // Handle Hardware & Gesture Back Button
    BackHandler(enabled = state.currentTab != AppTab.HOME || state.selectedTour != null || state.selectedDestination != null) {
        when {
            state.selectedDestination != null -> viewModel.selectDestination(null)
            state.selectedTour != null -> viewModel.closeTour()
            state.currentTab != AppTab.HOME -> viewModel.setTab(AppTab.HOME)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopHeritageBar(
                currentLanguage = state.language,
                onLanguageChange = { viewModel.setLanguage(it) },
                onAdminClick = { viewModel.setTab(AppTab.ADMIN) },
                onSearchClick = { showSearchDialog = true }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = state.currentTab,
                onTabSelected = { tab ->
                    if (state.selectedDestination != null) viewModel.selectDestination(null)
                    viewModel.setTab(tab)
                },
                language = state.language
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (state.currentTab) {
                AppTab.HOME -> {
                    HomeScreen(
                        state = state,
                        onTabSelected = { viewModel.setTab(it) },
                        onSelectDestination = { viewModel.selectDestination(it) },
                        onToggleBookmark = { viewModel.toggleBookmarkDestination(it) },
                        onCategoryFilterChange = { viewModel.setCategoryFilter(it) }
                    )
                }

                AppTab.AR_EXPERIENCE -> {
                    ArHeritageScreen(
                        language = state.language,
                        onNavigateBack = { viewModel.setTab(AppTab.HOME) }
                    )
                }

                AppTab.GALLERY_3D -> {
                    Gallery3DScreen(
                        language = state.language,
                        selectedModel = state.selectedModel,
                        onSelectModel = { viewModel.selectModel(it) },
                        onLaunchAr = { viewModel.setTab(AppTab.AR_EXPERIENCE) }
                    )
                }

                AppTab.TOURS_MAP -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Sub-tab switcher between Virtual Tours and Interactive Map
                        TabRow(
                            selectedTabIndex = toursMapSubTab,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = SaffronPrimary
                        ) {
                            Tab(
                                selected = toursMapSubTab == 0,
                                onClick = { toursMapSubTab = 0 },
                                text = { Text("Virtual Tours (5)", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = toursMapSubTab == 1,
                                onClick = { toursMapSubTab = 1 },
                                text = { Text("Interactive Map", fontWeight = FontWeight.Bold) }
                            )
                        }

                        if (toursMapSubTab == 0) {
                            VirtualToursScreen(
                                state = state,
                                onStartTour = { viewModel.startTour(it) },
                                onNextStop = { viewModel.nextTourStop() },
                                onPrevStop = { viewModel.prevTourStop() },
                                onCloseTour = { viewModel.closeTour() }
                            )
                        } else {
                            HeritageMapScreen(
                                language = state.language,
                                onSelectDestination = { viewModel.selectDestination(it) }
                            )
                        }
                    }
                }

                AppTab.AI_GUIDE -> {
                    AiGuideScreen(
                        state = state,
                        onSendMessage = { viewModel.sendAiPrompt(it) }
                    )
                }

                AppTab.CULTURE -> {
                    CultureScreen(
                        language = state.language,
                        bookmarkedIds = state.bookmarkedArticleIds,
                        onToggleBookmark = { viewModel.toggleBookmarkArticle(it) }
                    )
                }

                AppTab.QUIZ -> {
                    QuizScreen(
                        state = state,
                        questions = viewModel.getFilteredQuestions(),
                        onSelectDifficulty = { viewModel.setQuizDifficulty(it) },
                        onSelectOption = { viewModel.selectQuizOption(it) },
                        onSubmitAnswer = { viewModel.submitQuizAnswer() },
                        onNextQuestion = { viewModel.nextQuizQuestion() },
                        onResetQuiz = { viewModel.resetQuiz() }
                    )
                }

                AppTab.EXHIBITION -> {
                    ExhibitionScreen(language = state.language)
                }

                AppTab.PROFILE -> {
                    ProfileScreen(
                        state = state,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onSubmitFeedback = { name, cat, rating, comm ->
                            viewModel.submitFeedback(name, cat, rating, comm)
                        },
                        onDeleteFeedback = { viewModel.deleteFeedback(it) }
                    )
                }

                AppTab.ADMIN -> {
                    AdminScreen(
                        state = state,
                        onPinChange = { viewModel.updateAdminPin(it) },
                        onVerifyPin = { viewModel.verifyAdminPin() },
                        onLogout = { viewModel.logoutAdmin() },
                        onToggleVerification = { viewModel.toggleDestinationVerification(it) },
                        onDeleteFeedback = { viewModel.deleteFeedback(it) }
                    )
                }
            }

            // Global Destination Detail Dialog
            state.selectedDestination?.let { dest ->
                DestinationDetailDialog(
                    destination = dest,
                    language = state.language,
                    isBookmarked = state.bookmarkedDestinationIds.contains(dest.id),
                    onToggleBookmark = { viewModel.toggleBookmarkDestination(dest.id) },
                    onDismiss = { viewModel.selectDestination(null) }
                )
            }

            // Global Search Dialog
            if (showSearchDialog) {
                AlertDialog(
                    onDismissRequest = { showSearchDialog = false },
                    title = { Text("Search Godavari Virasat", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Search temples, ghats, history...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (state.searchQuery.isNotBlank()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Text(
                                text = "Found ${state.managedDestinations.count { it.nameEn.contains(state.searchQuery, ignoreCase = true) || it.nameTe.contains(state.searchQuery, ignoreCase = true) }} matching destinations.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSearchDialog = false
                                viewModel.setTab(AppTab.HOME)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                        ) {
                            Text("View on Dashboard")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSearchDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}
