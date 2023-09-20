package com.android.ar.ruler.ui.activities

import android.app.Application
import com.android.ar.ruler.advertisement.AppOpenManager
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp: Application() {

    private var appOpenManager: AppOpenManager? = null

    override fun onCreate() {
        super.onCreate()
        try {
            // Initialize the Google Mobile Ads SDK
            MobileAds.initialize(this)
            // uncomment after adding json file from firebase
            // FirebaseApp.initializeApp(this)
            appOpenManager = AppOpenManager(this)
        } catch (_: Exception) {
        } catch (_: VerifyError) {
        }
    }

}