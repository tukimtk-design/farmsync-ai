package com.tukimtk.farmsync.i18n

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    TH("th", "ภาษาไทย", "🇹🇭"),
    EN("en", "English", "🇬🇧")
}

object Strings {
    var currentLanguage by mutableStateOf(AppLanguage.TH)

    fun get(th: String, en: String): String {
        return if (currentLanguage == AppLanguage.TH) th else en
    }
}
