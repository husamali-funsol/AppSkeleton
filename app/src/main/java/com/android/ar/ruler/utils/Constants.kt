package com.android.ar.ruler.utils

class Constants {

    companion object {

        /**
         * SharedPreference Constants
         */
        const val PREF_KEY = "AntiTheftPref"
        const val PREF_PREMIUM = "AntiTheftPremiumPref"

        /**
         * Pin Code Constants
         */
        const val IS_PIN_CREATED = "isPinCreated"
        const val AUTH_PIN = "AuthenticationPin"

        /**
         * Premium Class Constant
         */
        const val SUBS_KEY = "gold"
        const val USER_PREMIUM = "USER_PREMIUM"

        /**
         * Google Ads Class Constant
         */
        var INTERSTITIAL_AD_CAPPING = 30
        var INTERSTITIAL_AD_STRATEGY = 1

        var NATIVE_AD_STRATEGY = 1
        var SHOW_SPLASH_NATIVE_AD = 1
        var SHOW_ONBOARDING_SCREEN = 1
        var SHOW_ONBOARDING_NATIVE_AD = 1
        var NATIVE_ONBOARDING_ROUND_PERCENT: Float = 0.3F
        var NATIVE_ONBOARDING_BTN_COLOR: String = "#D80D0D"

        @JvmField
        var APP_OPEN_AD_STRATEGY = 1

        /**
         * Locale Constant
         */
        const val NIGHT_MODE = "NightMode"
        const val LANG_NAME = "LangName"
        var CHANGE_LANGUAGE = "ChangeLanguage"
        const val SHOW_LANGUAGE_SCREEN = "ShowLanguageScreen"

        /**
         * Others
         */

        var SHOW_EXIT_DIALOG_NATIVE_AD = 1

    }
}