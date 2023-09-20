package com.android.ar.ruler.utils

import com.android.ar.ruler.models.LanguagesModel
import java.util.*

val languages: ArrayList<LanguagesModel>
    get() {
        val arrayList = ArrayList<LanguagesModel>()
        arrayList.add(LanguagesModel("English", "en", Locale("en", "US")))
        arrayList.add(LanguagesModel("Arabic", "ar", Locale("ar", "SA")))
        arrayList.add(LanguagesModel("Russian", "ru", Locale("ru", "RU")))
        arrayList.add(LanguagesModel("Indonesian", "in", Locale("id", "ID")))
        arrayList.add(LanguagesModel("Bengali", "bn", Locale("bn", "BD")))
        arrayList.add(LanguagesModel("Hindi", "hi", Locale("hi", "IN")))
        arrayList.add(LanguagesModel("Ukrainian", "uk", Locale("uk", "UA")))
        arrayList.add(LanguagesModel("Vietnamese", "vi", Locale("vi", "VN")))
        arrayList.add(LanguagesModel("Korean", "ko", Locale("ko", "KR")))
        arrayList.add(LanguagesModel("Japanese", "ja", Locale("ja", "JP")))
        arrayList.add(LanguagesModel("Chinese", "zh", Locale("ja", "JP")))
        arrayList.add(LanguagesModel("Swedish", "sv", Locale("sv", "SE")))
        arrayList.add(LanguagesModel("Polish", "pl", Locale("pl", "PL")))
        arrayList.add(LanguagesModel("Malay", "ms", Locale("ms", "MY")))
        arrayList.add(LanguagesModel("French", "fr", Locale("fr", "FR")))
        arrayList.add(LanguagesModel("Italian", "it", Locale("it", "IT")))
        arrayList.add(LanguagesModel("Persian", "fa", Locale("fa", "IR")))
        arrayList.add(LanguagesModel("Turkish", "tr", Locale("tr", "TR")))
        arrayList.add(LanguagesModel("Thai", "th", Locale("th", "TH")))
        arrayList.add(LanguagesModel("Portuguese", "pt", Locale("pt", "PT")))
        arrayList.add(LanguagesModel("Spanish", "es", Locale("es", "ES")))
        arrayList.add(LanguagesModel("German", "de", Locale("de", "DE")))
        arrayList.add(LanguagesModel("Netherlands Dutch", "nl", Locale("nl", "NL")))
        arrayList.add(LanguagesModel("Tamil", "ta", Locale("ta", "IN")))
        arrayList.add(LanguagesModel("Czech", "cs", Locale("cs", "")))
        arrayList.add(LanguagesModel("Urdu", "ur", Locale("ur", "IN")))
        arrayList.sortBy { it.name }
        return arrayList
    }