package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.HeritageRepository
import com.example.data.local.AppDatabase
import com.example.data.local.FeedbackEntity
import com.example.data.local.UserProgressEntity
import com.example.model.*
import com.example.ui.components.AppTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AppUiState(
    val currentTab: AppTab = AppTab.HOME,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val searchQuery: String = "",
    val destinationCategoryFilter: String = "All",
    val selectedDestination: HeritageDestination? = null,
    val selectedModel: Model3DItem = HeritageRepository.model3DItems.first(),
    val selectedTour: VirtualTour? = null,
    val currentTourStopIndex: Int = 0,
    val completedTourIds: Set<String> = emptySet(),
    val bookmarkedDestinationIds: Set<String> = emptySet(),
    val bookmarkedArticleIds: Set<String> = emptySet(),
    val earnedBadges: Set<String> = setOf("Pushkaralu Pilgrim"),
    // Quiz state
    val quizDifficulty: String = "Novice",
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val quizScore: Int = 0,
    val isQuizFinished: Boolean = false,
    val pastQuizHighScores: List<Int> = emptyList(),
    // AI Chat state
    val chatMessages: List<ChatMessage> = emptyList(),
    val isAiThinking: Boolean = false,
    // Admin state
    val isAdminAuthenticated: Boolean = false,
    val adminPinInput: String = "",
    val adminError: String? = null,
    val managedDestinations: List<HeritageDestination> = HeritageRepository.destinations,
    // User feedback
    val feedbackList: List<FeedbackEntity> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application.applicationContext,
        AppDatabase::class.java,
        "godavari_virasat.db"
    ).fallbackToDestructiveMigration().build()

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        loadSavedPreferences()
        observeFeedback()
        initAiWelcomeMessage(AppLanguage.ENGLISH)
    }

    private fun loadSavedPreferences() {
        viewModelScope.launch {
            val savedLangCode = db.userProgressDao().getValue("app_language")
            if (savedLangCode != null) {
                val lang = AppLanguage.entries.find { it.code == savedLangCode } ?: AppLanguage.ENGLISH
                _uiState.update { it.copy(language = lang) }
                initAiWelcomeMessage(lang)
            }
        }
    }

    private fun observeFeedback() {
        viewModelScope.launch {
            db.feedbackDao().getAllFeedback().collect { list ->
                _uiState.update { it.copy(feedbackList = list) }
            }
        }
    }

    fun setTab(tab: AppTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setLanguage(lang: AppLanguage) {
        _uiState.update { it.copy(language = lang) }
        viewModelScope.launch {
            db.userProgressDao().savePreference(UserProgressEntity("app_language", lang.code))
        }
        initAiWelcomeMessage(lang)
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setCategoryFilter(category: String) {
        _uiState.update { it.copy(destinationCategoryFilter = category) }
    }

    fun selectDestination(destination: HeritageDestination?) {
        _uiState.update { it.copy(selectedDestination = destination) }
    }

    fun selectModel(model: Model3DItem) {
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun startTour(tour: VirtualTour) {
        _uiState.update {
            it.copy(
                selectedTour = tour,
                currentTourStopIndex = 0
            )
        }
    }

    fun nextTourStop() {
        _uiState.update { state ->
            val tour = state.selectedTour ?: return@update state
            if (state.currentTourStopIndex < tour.stops.size - 1) {
                state.copy(currentTourStopIndex = state.currentTourStopIndex + 1)
            } else {
                // Tour finished
                val updatedTours = state.completedTourIds + tour.id
                val updatedBadges = if (updatedTours.size >= 3) state.earnedBadges + "Heritage Explorer" else state.earnedBadges
                state.copy(
                    completedTourIds = updatedTours,
                    earnedBadges = updatedBadges
                )
            }
        }
    }

    fun prevTourStop() {
        _uiState.update { state ->
            if (state.currentTourStopIndex > 0) {
                state.copy(currentTourStopIndex = state.currentTourStopIndex - 1)
            } else state
        }
    }

    fun closeTour() {
        _uiState.update { it.copy(selectedTour = null, currentTourStopIndex = 0) }
    }

    fun toggleBookmarkDestination(id: String) {
        _uiState.update { state ->
            val bookmarks = if (state.bookmarkedDestinationIds.contains(id)) {
                state.bookmarkedDestinationIds - id
            } else {
                state.bookmarkedDestinationIds + id
            }
            state.copy(bookmarkedDestinationIds = bookmarks)
        }
    }

    fun toggleBookmarkArticle(id: String) {
        _uiState.update { state ->
            val bookmarks = if (state.bookmarkedArticleIds.contains(id)) {
                state.bookmarkedArticleIds - id
            } else {
                state.bookmarkedArticleIds + id
            }
            state.copy(bookmarkedArticleIds = bookmarks)
        }
    }

    // Quiz Methods
    fun setQuizDifficulty(difficulty: String) {
        _uiState.update {
            it.copy(
                quizDifficulty = difficulty,
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                isAnswerSubmitted = false,
                quizScore = 0,
                isQuizFinished = false
            )
        }
    }

    fun selectQuizOption(index: Int) {
        if (!_uiState.value.isAnswerSubmitted && !_uiState.value.isQuizFinished) {
            _uiState.update { it.copy(selectedOptionIndex = index) }
        }
    }

    fun submitQuizAnswer() {
        val state = _uiState.value
        val questions = getFilteredQuestions()
        if (state.selectedOptionIndex == null || state.isAnswerSubmitted || state.currentQuestionIndex >= questions.size) return

        val currentQ = questions[state.currentQuestionIndex]
        val isCorrect = state.selectedOptionIndex == currentQ.correctIndex
        val newScore = if (isCorrect) state.quizScore + 1 else state.quizScore

        _uiState.update {
            it.copy(
                isAnswerSubmitted = true,
                quizScore = newScore
            )
        }
    }

    fun nextQuizQuestion() {
        val state = _uiState.value
        val questions = getFilteredQuestions()
        if (state.currentQuestionIndex < questions.size - 1) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    selectedOptionIndex = null,
                    isAnswerSubmitted = false
                )
            }
        } else {
            // Quiz finished
            val newBadges = state.earnedBadges.toMutableSet()
            if (state.quizScore >= (questions.size * 0.7)) {
                newBadges.add("Godavari Scholar")
            }
            if (state.quizScore == questions.size) {
                newBadges.add("Pushkaralu Master")
            }
            _uiState.update {
                it.copy(
                    isQuizFinished = true,
                    pastQuizHighScores = it.pastQuizHighScores + it.quizScore,
                    earnedBadges = newBadges
                )
            }
        }
    }

    fun resetQuiz() {
        _uiState.update {
            it.copy(
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                isAnswerSubmitted = false,
                quizScore = 0,
                isQuizFinished = false
            )
        }
    }

    fun getFilteredQuestions(): List<QuizQuestion> {
        val diff = _uiState.value.quizDifficulty
        return HeritageRepository.quizQuestions.filter { it.difficulty == diff }
    }

    // AI Chat Assistant
    private fun initAiWelcomeMessage(lang: AppLanguage) {
        val welcomeText = when (lang) {
            AppLanguage.TELUGU -> "నమస్కారం! నేను గోదావరి విరాసత్ ఏఐ మార్గదర్శిని. గోదావరి చరిత్ర, పుష్కరాలు 2027, ప్రాచీన ఆలయాలు, సర్ ఆర్థర్ కాటన్ ఆనకట్ట మరియు కోనసీమ సంస్కృతి గురించి నన్ను ఏమైనా అడగండి."
            AppLanguage.HINDI -> "नमस्ते! मैं गोदावरी विरासत एआई मार्गदर्शक हूँ। गोदावरी का इतिहास, पुष्करालु 2027, प्राचीन मंदिर, संस्कृति और यात्रा के बारे में कोई भी प्रश्न पूछें।"
            AppLanguage.ENGLISH -> "Namaste! I am the Godavari Virasat AI Heritage Guide. Ask me anything about Godavari history, Pushkaralu 2027, ancient temples, Sir Arthur Cotton's delta engineering, or local art and culture."
        }
        val msg = ChatMessage(
            id = "welcome",
            text = welcomeText,
            isUser = false,
            language = lang,
            references = listOf("Godavari Virasat Cultural Knowledge Base", "Skanda Purana", "State Irrigation Archives"),
            verificationNotice = "VERIFIED REGIONAL HERITAGE DATA"
        )
        _uiState.update { it.copy(chatMessages = listOf(msg)) }
    }

    fun sendAiPrompt(prompt: String) {
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = prompt,
            isUser = true,
            language = _uiState.value.language
        )
        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + userMsg,
                isAiThinking = true
            )
        }

        viewModelScope.launch {
            kotlinx.coroutines.delay(650) // Natural conversational response pace
            val responseText = generateHeritageResponse(prompt, _uiState.value.language)
            val aiMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                text = responseText.first,
                isUser = false,
                language = _uiState.value.language,
                references = responseText.second,
                verificationNotice = "CURATED HERITAGE ARCHIVE (DEMO NOTIFICATION: UNVERIFIED FACTS SHOULD BE RE-CHECKED WITH OFFICIAL ARCHAEOLOGICAL SOURCES)"
            )
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + aiMsg,
                    isAiThinking = false
                )
            }
        }
    }

    private fun generateHeritageResponse(query: String, lang: AppLanguage): Pair<String, List<String>> {
        val lower = query.lowercase()
        return when {
            lower.contains("pushkar") || lower.contains("పుష్కర") || lower.contains("पुष्कर") -> {
                val ans = when (lang) {
                    AppLanguage.TELUGU -> "గోదావరి పుష్కరాలు ప్రతి 12 సంవత్సరాలకు ఒకసారి బృహస్పతి సింహరాశిలోకి ప్రవేశించినప్పుడు జరుగుతాయి. తదుపరి పుష్కరాలు 2027వ సంవత్సరంలో అత్యంత వైభవంగా జరగనున్నాయి. ఈ పవిత్ర సమయంలో కోటిలింగాల ఘాట్, పుష్కర ఘాట్, భద్రాచలం వద్ద పవిత్ర స్నానాలు ఆచరిస్తారు."
                    AppLanguage.HINDI -> "गोदावरी पुष्करालु प्रत्येक 12 वर्ष में जब बृहस्पति सिंह राशि में प्रवेश करते हैं तब आयोजित होता है। अगला महाकुंभ वर्ष 2027 में होगा, जिसमें राजमुंदरी, भद्राचलम और बासर में लाखों श्रद्धालु पावन स्नान करेंगे।"
                    AppLanguage.ENGLISH -> "Godavari Pushkaralu occurs once every 12 years when Jupiter (Brihaspati) transits into Leo (Simha Rasi). The upcoming celebration is slated for 2027. Pilgrims take holy dips at Kotilingala Ghat, Pushkar Ghat, Bhadrachalam, and Antarvedi confluence."
                }
                ans to listOf("Skanda Purana", "Ephemeris Astronomical Tables", "Godavari Virasat Records")
            }
            lower.contains("cotton") || lower.contains("barrage") || lower.contains("కాటన్") || lower.contains("ఆనకట్ట") || lower.contains("कॉटन") -> {
                val ans = when (lang) {
                    AppLanguage.TELUGU -> "సర్ ఆర్థర్ కాటన్ 1852లో ధవళేశ్వరం వద్ద 3.6 కి.మీ పొడవైన గోదావరి ఆనకట్టను నిర్మించారు. అంతకుముందు కరవు కాటకాలతో అల్లాడిన గోదావరి డెల్టాను దేశానికే అన్నపూర్ణగా మార్చారు. ఇక్కడి రైతులు ఆయనను 'కాటన్ దొర'గా గుండెల్లో పెట్టుకుని పూజిస్తారు."
                    AppLanguage.HINDI -> "सर आर्थर कॉटन ने 1852 में धवलेश्वरम में 3.6 किमी लंबे बैराज का निर्माण किया। इससे भयंकर अकाल समाप्त हुआ और गोदावरी डेल्टा भारत का सबसे उपजाऊ क्षेत्र बन गया।"
                    AppLanguage.ENGLISH -> "Sir Arthur Cotton engineered the historic 3.6 km masonry weir at Dowleswaram, commissioned in 1852. Defying colonial skepticism, his innovative hydraulic design channeled water into three major canal systems, turning famine-stricken lands into the green rice bowl of South India."
                }
                ans to listOf("Dowleswaram Irrigation Museum", "Madras Presidency Public Works Despatches 1847-1852")
            }
            lower.contains("pancharama") || lower.contains("పంచారామ") || lower.contains("पंचाराम") || lower.contains("draksharam") -> {
                val ans = when (lang) {
                    AppLanguage.TELUGU -> "పంచారామాలలో ద్రాక్షారామ భీమేశ్వర స్వామి ఆలయం ప్రధానమైనది. ఇక్కడ 14 అడుగుల ఎత్తైన స్ఫటిక లింగం రెండు అంతస్తుల వరకు ఉంటుంది. ఇది అష్టాదశ శక్తిపీఠాలలో మాణిక్యాంబా దేవి పీఠం కూడా. మిగిలినవి క్షీరారామం (పాలకొల్లు), సోమారామం (భీమవరం), కుమారారామం (సామర్లకోట), అమరారామం (అమరావతి)."
                    AppLanguage.HINDI -> "पंचाराम क्षेत्रों में द्राक्षाराम भीमेश्वर स्वामी अत्यंत पवित्र है, जहां 14 फीट ऊंचा स्फटिक शिवलिंग दो मंजिलों तक विस्तृत है। यह अष्टादश महाशक्तिपीठों में से भी एक है।"
                    AppLanguage.ENGLISH -> "The Pancharama Kshetras are five sacred Shiva shrines where fragments of the Tarakasura lingam were consecrated: Draksharamam (Dakshina Kashi), Somaramam (Bhimavaram), Ksheeraramam (Palakollu), Kumararamam (Samalkot), and Amararamam (Amaravathi). Draksharamam is uniquely both a Pancharama and a Maha Shakti Peetha (Manikyamba Devi)."
                }
                ans to listOf("Eastern Chalukya Inscriptions (Chalukya Bhima I)", "Shiva Purana", "ASI Archaeological Surveys")
            }
            lower.contains("bhadrachalam") || lower.contains("రాముడు") || lower.contains("రామదాసు") || lower.contains("भद्राचलम") -> {
                val ans = when (lang) {
                    AppLanguage.TELUGU -> "భద్రాచలం వద్ద గోదావరి ఉత్తర వాహినిగా ప్రవహిస్తుంది. 17వ శతాబ్దంలో కంచెర్ల గోపన్న (భక్త రామదాసు) ఇక్కడ దివ్య సీతారామచంద్ర స్వామి ఆలయాన్ని నిర్మించారు. ఇక్కడ రాముడు చతుర్భుజుడై శంఖ చక్రాలు ధరించి దర్శనమిస్తాడు."
                    AppLanguage.HINDI -> "भद्राचलम में गोदावरी उत्तरवाहिनी होकर बहती है। 17वीं सदी में भक्त रामदास ने इस भव्य मंदिर का निर्माण कराया। यहां भगवान राम चतुर्भुज रूप में धनुष, बाण, शंख और चक्र के साथ विराजमान हैं।"
                    AppLanguage.ENGLISH -> "Bhadrachalam is venerated as Southern Ayodhya along the north-flowing (Uttaravahini) bend of Godavari. Built by devotee-saint Bhakta Ramadasu in the 17th century, it is unique because Lord Rama appears with four arms holding the sacred conch (Panchajanya) and discus (Sudarshana)."
                }
                ans to listOf("Ramadasu Charitra", "Bhadradri Kshetra Mahatmyam")
            }
            else -> {
                val ans = when (lang) {
                    AppLanguage.TELUGU -> "గోదావరి నది భారతీయ సంస్కృతికి జీవనాడి. మహారాష్ట్రలోని త్రయంబకేశ్వరంలో జన్మించి 1,465 కి.మీ ప్రయాణించి అంతర్వేది వద్ద బంగాళాఖాతంలో కలుస్తుంది. గోదావరి తీరంలో శిల్పకళ, చేనేత, సాహిత్య సాంప్రదాయాలు అపారమైనవి. మరింత సమాచారం కోసం నిర్దిష్ట అంశాన్ని అడగండి."
                    AppLanguage.HINDI -> "गोदावरी नदी भारत की पावन जीवनधारा है। नासिक के त्र्यंबकेश्वर से निकलकर 1,465 किमी बहती हुई यह बंगाल की खाड़ी में समाहित होती है। इसके तट पर स्थित कला, संस्कृति और मंदिरों के बारे में आप विस्तार से जान सकते हैं।"
                    AppLanguage.ENGLISH -> "River Godavari, known as the Dakshina Ganga, spans 1,465 km from Brahmagiri in Trimbakeshwar to the Bay of Bengal at Antarvedi. It sustains deep spiritual, architectural, handloom, and culinary heritages. Feel free to ask about specific ghats, boat heritage, or 2027 Pushkaralu preparation!"
                }
                ans to listOf("Godavari Cultural Heritage Registry", "Regional Gazetteer")
            }
        }
    }

    // Feedback
    fun submitFeedback(name: String, category: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val entity = FeedbackEntity(
                pilgrimName = name.ifBlank { "Pilgrim" },
                category = category,
                rating = rating,
                comment = comment,
                timestamp = System.currentTimeMillis()
            )
            db.feedbackDao().insertFeedback(entity)
        }
    }

    fun deleteFeedback(id: Long) {
        viewModelScope.launch {
            db.feedbackDao().deleteFeedback(id)
        }
    }

    // Admin Panel
    fun updateAdminPin(pin: String) {
        _uiState.update { it.copy(adminPinInput = pin, adminError = null) }
    }

    fun verifyAdminPin(): Boolean {
        // Default secure demonstration admin PIN
        if (_uiState.value.adminPinInput == "2027") {
            _uiState.update { it.copy(isAdminAuthenticated = true, adminError = null) }
            return true
        } else {
            _uiState.update { it.copy(adminError = "Invalid Admin PIN. Use '2027' for demonstration access.") }
            return false
        }
    }

    fun logoutAdmin() {
        _uiState.update { it.copy(isAdminAuthenticated = false, adminPinInput = "") }
    }

    fun toggleDestinationVerification(id: String) {
        _uiState.update { state ->
            val updated = state.managedDestinations.map { dest ->
                if (dest.id == id) {
                    val newStatus = if (dest.verificationStatus == "VERIFIED") "DEMO" else "VERIFIED"
                    dest.copy(
                        verificationStatus = newStatus,
                        isDemo = newStatus == "DEMO"
                    )
                } else dest
            }
            state.copy(managedDestinations = updated)
        }
    }
}
