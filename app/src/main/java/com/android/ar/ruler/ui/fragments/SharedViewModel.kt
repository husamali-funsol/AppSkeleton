package com.android.ar.ruler.ui.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    // variable to contain message whenever
    // it gets changed/modified(mutable)
    private val _message = MutableLiveData<String>().apply {
        value = "Press below button to shared data between fragments"
    }
    val message: LiveData<String> = _message

    // function to send message
    fun sendMessage(text: Int) {
        _message.value = text.toString()
    }
}