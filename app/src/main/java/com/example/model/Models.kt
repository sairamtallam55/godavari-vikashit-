package com.example.model

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    TELUGU("te", "Telugu", "తెలుగు"),
    HINDI("hi", "Hindi", "हिंदी")
}

data class HeritageDestination(
    val id: String,
    val nameEn: String,
    val nameTe: String,
    val nameHi: String,
    val category: String, // Sacred Ghat, Ancient Temple, Colonial Engineering, River Valley, Craft Village
    val district: String,
    val state: String = "Andhra Pradesh",
    val latitude: Double,
    val longitude: Double,
    val era: String,
    val descriptionEn: String,
    val descriptionTe: String,
    val descriptionHi: String,
    val highlights: List<String>,
    val drawableResName: String? = null,
    val isDemo: Boolean = true,
    val verificationStatus: String = "DEMO" // "DEMO" or "VERIFIED"
)

enum class Model3DType {
    SHIVALINGAM,
    BRIDGE_ARCH,
    SLUICE_GATE,
    TEMPLE_GOPURAM,
    HARATHI_LAMP,
    MONOLITHIC_PILLAR,
    HOUSEBOAT,
    WOODEN_TOY
}

data class Model3DItem(
    val id: String,
    val nameEn: String,
    val nameTe: String,
    val nameHi: String,
    val modelType: Model3DType,
    val category: String,
    val era: String,
    val discoveryLocation: String,
    val descriptionEn: String,
    val descriptionTe: String,
    val descriptionHi: String,
    val architecturalSignificance: String,
    val isDemo: Boolean = true,
    val verificationStatus: String = "DEMO"
)

data class TourStop(
    val stopNumber: Int,
    val titleEn: String,
    val titleTe: String,
    val titleHi: String,
    val descriptionEn: String,
    val descriptionTe: String,
    val descriptionHi: String,
    val keySight: String,
    val ritualGuideline: String
)

data class VirtualTour(
    val id: String,
    val titleEn: String,
    val titleTe: String,
    val titleHi: String,
    val region: String,
    val estimatedMinutes: Int,
    val stops: List<TourStop>,
    val isDemo: Boolean = true,
    val verificationStatus: String = "DEMO"
)

data class CulturalArticle(
    val id: String,
    val titleEn: String,
    val titleTe: String,
    val titleHi: String,
    val category: String,
    val readTimeMinutes: Int,
    val summaryEn: String,
    val summaryTe: String,
    val summaryHi: String,
    val contentEn: String,
    val contentTe: String,
    val contentHi: String,
    val traditions: List<String>,
    val isDemo: Boolean = true,
    val verificationStatus: String = "DEMO"
)

data class ExhibitionItem(
    val id: String,
    val titleEn: String,
    val titleTe: String,
    val titleHi: String,
    val yearEra: String,
    val category: String,
    val descriptionEn: String,
    val descriptionTe: String,
    val descriptionHi: String,
    val archivalNote: String,
    val isDemo: Boolean = true,
    val verificationStatus: String = "DEMO"
)

data class QuizQuestion(
    val id: Int,
    val questionEn: String,
    val questionTe: String,
    val questionHi: String,
    val optionsEn: List<String>,
    val optionsTe: List<String>,
    val optionsHi: List<String>,
    val correctIndex: Int,
    val explanationEn: String,
    val explanationTe: String,
    val explanationHi: String,
    val difficulty: String, // "Novice" or "Scholar"
    val topic: String,
    val isDemo: Boolean = true,
    val verificationStatus: String = "DEMO"
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val language: AppLanguage = AppLanguage.ENGLISH,
    val references: List<String> = emptyList(),
    val verificationNotice: String? = null
)

data class FeedbackSubmission(
    val id: Long = 0,
    val pilgrimName: String,
    val category: String,
    val rating: Int,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
