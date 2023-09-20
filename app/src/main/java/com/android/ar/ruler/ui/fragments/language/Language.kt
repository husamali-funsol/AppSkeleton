package com.android.ar.ruler.ui.fragments.language

import androidx.annotation.Keep

@Keep
data class Language(
    val countryCode: String,
    val languageName: String,
    val languageCode: String = "en",
    val flag: String
)