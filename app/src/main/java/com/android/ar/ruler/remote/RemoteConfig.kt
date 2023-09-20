package com.android.ar.ruler.remote

import android.app.Activity
import android.util.Log
import com.android.ar.ruler.R
import com.android.ar.ruler.utils.Constants.Companion.APP_OPEN_AD_STRATEGY
import com.android.ar.ruler.utils.Constants.Companion.INTERSTITIAL_AD_CAPPING
import com.android.ar.ruler.utils.Constants.Companion.INTERSTITIAL_AD_STRATEGY
import com.android.ar.ruler.utils.Constants.Companion.NATIVE_AD_STRATEGY
import com.android.ar.ruler.utils.Constants.Companion.NATIVE_ONBOARDING_BTN_COLOR
import com.android.ar.ruler.utils.Constants.Companion.NATIVE_ONBOARDING_ROUND_PERCENT
import com.android.ar.ruler.utils.Constants.Companion.SHOW_EXIT_DIALOG_NATIVE_AD
import com.android.ar.ruler.utils.Constants.Companion.SHOW_ONBOARDING_NATIVE_AD
import com.android.ar.ruler.utils.Constants.Companion.SHOW_ONBOARDING_SCREEN
import com.android.ar.ruler.utils.Constants.Companion.SHOW_SPLASH_NATIVE_AD
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

fun remoteConfig(activity: Activity,onCompleted: ()->Unit) {
    val mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(3600).build()
    mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
    mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remoteapp)

    mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(activity){task->
        if (task.isSuccessful){
            val updated = task.result
            Log.d("REMOTE_TAG", "Config params updated: $updated")

            /**Interstitial Ads*/
            INTERSTITIAL_AD_STRATEGY = mFirebaseRemoteConfig.getLong("interstitial_ad_strategy").toInt()
            INTERSTITIAL_AD_CAPPING = mFirebaseRemoteConfig.getLong("interstitial_ad_capping").toInt()

            if (INTERSTITIAL_AD_CAPPING<30){
                INTERSTITIAL_AD_CAPPING = 30
            }

            /**Native Ads*/
            NATIVE_AD_STRATEGY = mFirebaseRemoteConfig.getLong("native_ad_strategy").toInt()
            SHOW_EXIT_DIALOG_NATIVE_AD = mFirebaseRemoteConfig.getLong("show_exit_dialog_native_Ad").toInt()

            //Splash Screen
            SHOW_SPLASH_NATIVE_AD = mFirebaseRemoteConfig.getLong("show_splash_native_ad").toInt()

            //OnBoarding Screen
            SHOW_ONBOARDING_SCREEN = mFirebaseRemoteConfig.getLong("show_onboarding_screen").toInt()
            SHOW_ONBOARDING_NATIVE_AD = mFirebaseRemoteConfig.getLong("show_onboarding_native_ad").toInt()
            NATIVE_ONBOARDING_ROUND_PERCENT = mFirebaseRemoteConfig.getString("native_onboarding_round_percent").toFloat()
            NATIVE_ONBOARDING_BTN_COLOR = mFirebaseRemoteConfig.getString("native_onboarding_btn_color")

            /***App Open Ad*/
            APP_OPEN_AD_STRATEGY = mFirebaseRemoteConfig.getLong("app_open_ad_strategy").toInt()

            onCompleted.invoke()
        }
    }
}
