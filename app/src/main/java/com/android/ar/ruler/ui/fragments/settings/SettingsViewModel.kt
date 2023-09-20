package com.android.ar.ruler.ui.fragments.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class SettingsViewModel : ViewModel() {

    //Note: its not observe the repeated same values
    private val _text = MutableStateFlow("This is setting Fragment")

    val text: MutableStateFlow<String> = _text

}