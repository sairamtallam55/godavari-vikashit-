package com.example.ui.components

import com.example.model.AppLanguage

object UiStrings {
    fun appTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "గోదావరి విరాసత్"
        AppLanguage.HINDI -> "गोदावरी विरासत"
        AppLanguage.ENGLISH -> "Godavari Virasat"
    }

    fun tagline(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "వారసత్వాన్ని అనుభవించండి. గోదావరిని అన్వేషించండి."
        AppLanguage.HINDI -> "विरासत का अनुभव करें। गोदावरी का अन्वेषण करें।"
        AppLanguage.ENGLISH -> "Experience the Heritage. Explore the Godavari."
    }

    fun pushkaraluNotice(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "గోదావరి పుష్కరాలు 2027 దివ్య సంకల్పం"
        AppLanguage.HINDI -> "गोदावरी पुष्करालु 2027 दिव्य संकल्प"
        AppLanguage.ENGLISH -> "Godavari Pushkaralu 2027 Heritage Mission"
    }

    // Navigation Labels
    fun navHome(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ప్రారంభం"
        AppLanguage.HINDI -> "गृह"
        AppLanguage.ENGLISH -> "Home"
    }

    fun navAr(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఏఆర్ అనుభవం"
        AppLanguage.HINDI -> "एआर दर्शन"
        AppLanguage.ENGLISH -> "AR Live"
    }

    fun nav3D(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "3డి గ్యాలరీ"
        AppLanguage.HINDI -> "3डी दीर्घा"
        AppLanguage.ENGLISH -> "3D Gallery"
    }

    fun navTours(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "యాత్రలు"
        AppLanguage.HINDI -> "यात्राएं"
        AppLanguage.ENGLISH -> "Tours"
    }

    fun navMap(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "పటం"
        AppLanguage.HINDI -> "मानचित्र"
        AppLanguage.ENGLISH -> "Map"
    }

    fun navAi(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ఏఐ మార్గదర్శి"
        AppLanguage.HINDI -> "एआई मार्गदर्शक"
        AppLanguage.ENGLISH -> "AI Guide"
    }

    fun navCulture(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "సంస్కృతి"
        AppLanguage.HINDI -> "संस्कृति"
        AppLanguage.ENGLISH -> "Culture"
    }

    fun navLearn(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "క్విజ్"
        AppLanguage.HINDI -> "प्रश्नोत्तरी"
        AppLanguage.ENGLISH -> "Quiz & Learn"
    }

    fun navExhibition(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ప్రదర్శన"
        AppLanguage.HINDI -> "प्रदर्शनी"
        AppLanguage.ENGLISH -> "Exhibition"
    }

    fun navProfile(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "ప్రొఫైల్"
        AppLanguage.HINDI -> "प्रोफ़ाइल"
        AppLanguage.ENGLISH -> "Profile"
    }

    fun navAdmin(lang: AppLanguage): String = when (lang) {
        AppLanguage.TELUGU -> "నిర్వాహకుడు"
        AppLanguage.HINDI -> "व्यवस्थापक"
        AppLanguage.ENGLISH -> "Admin"
    }
}
