package com.android.ar.ruler.ui.activities

import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    companion object {
        @JvmField
        var doNotShowAppOpenAd = false

        @JvmField
        var interstitialShown = false

        @JvmField
        var isActivityPause=false
    }

    override fun onPause() {
        isActivityPause=true
        super.onPause()
    }

    override fun onResume() {
        isActivityPause=false
        super.onResume()
    }

}