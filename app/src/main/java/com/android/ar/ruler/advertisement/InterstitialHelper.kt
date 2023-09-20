package com.android.ar.ruler.advertisement

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.ar.ruler.R
import com.android.ar.ruler.billing.BillingUtilsIAP
import com.android.ar.ruler.ui.activities.BaseActivity
import com.android.ar.ruler.ui.dialogs.LoadingDialog
import com.android.ar.ruler.utils.Constants
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.TimeUnit

object InterstitialHelper {
    private const val TAG = "interstitial_ad_log"
    private var interstitialTimeElapsed = 0L
    private var mInterstitialAd: InterstitialAd? = null
    private var mInterstitialAdSplash: InterstitialAd? = null
    private var isAdLoading = false
    var isShowing = false
    var adCappingBetweenInterstitialAndAppOpenAd = 0L

    /**
     * Inner Interstitial Load and Show Functions
     */

    fun showAndLoadInterstitial(activity: Activity, dismissCallback: (String) -> Unit) {
        if (!BillingUtilsIAP.isPremium && isNetworkAvailable(activity)
            && timeDifference(interstitialTimeElapsed) > Constants.INTERSTITIAL_AD_CAPPING
            && !isAdLoading
            && timeDifference(adCappingBetweenInterstitialAndAppOpenAd) > 10
        ) {
            LoadingDialog.showLoadingDialog(activity)
            if (mInterstitialAd != null) {
                BaseActivity.interstitialShown = true
                CoroutineScope(Dispatchers.IO).launch {
                    delay(700)
                    withContext(Dispatchers.Main) {
                        if (!BaseActivity.isActivityPause) {
                            mInterstitialAd?.show(activity)
                            mInterstitialAd?.fullScreenContentCallback =
                                object : FullScreenContentCallback() {
                                    override fun onAdImpression() {
                                        super.onAdImpression()
                                        Log.i(TAG, "Ad impression")
                                        isShowing = true
                                        mInterstitialAd = null

                                        LoadingDialog.hideLoadingDialog()
                                    }

                                    override fun onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent()
                                        Log.i(TAG, "Ad Dismiss")
                                        dismissCallback.invoke("")
                                        BaseActivity.interstitialShown = false
                                        isShowing = false
                                        val loadDelay: Long = (Constants.INTERSTITIAL_AD_CAPPING - 8L) * 1000L
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            loadInterstitialAd(activity)

                                        }, loadDelay)
                                        adCappingBetweenInterstitialAndAppOpenAd = Calendar.getInstance().timeInMillis
                                        interstitialTimeElapsed = Calendar.getInstance().timeInMillis
                                        LoadingDialog.hideLoadingDialog()
                                    }

                                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                        super.onAdFailedToShowFullScreenContent(p0)
                                        dismissCallback.invoke("failedToShow")
                                        isShowing = false
                                    }
                                }
                            LoadingDialog.hideLoadingDialog()
                        } else {
                            LoadingDialog.hideLoadingDialog()
                        }
                    }

                }
            } else {
                if (!isAdLoading) {
                    loadInterstitialAd(activity)
                }
                dismissCallback.invoke("")
                BaseActivity.interstitialShown = false
                LoadingDialog.hideLoadingDialog()
            }
        } else {
            dismissCallback.invoke("")
        }
    }

    fun loadInterstitialAd(activity: Activity) {
        if (!BillingUtilsIAP.isPremium && mInterstitialAd == null) {
            Log.i(TAG, "Ad Call with ID: ${activity.resources.getString(R.string.splash_interstitial_id)}")
            isAdLoading = true
            InterstitialAd.load(
                activity,
                activity.resources.getString(R.string.inner_interstitial_id),
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        BaseActivity.interstitialShown = false
                        Log.d(TAG, adError.message)
                        mInterstitialAd = null
                        isAdLoading = false
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.i(TAG, "Ad Loaded")
                        mInterstitialAd = interstitialAd
                        isAdLoading = false
                    }
                })
        }
    }

    /**
     * Splash Interstitial Load and Show Functions
     */

    fun showSplashInterstitial(activity: Activity, dismissCallback: (String) -> Unit) {
        if (!BillingUtilsIAP.isPremium && isNetworkAvailable(activity)) {
            LoadingDialog.showLoadingDialog(activity)

            if (mInterstitialAdSplash != null) {
                BaseActivity.interstitialShown = true
                CoroutineScope(Dispatchers.IO).launch {
                    delay(700)
                    withContext(Dispatchers.Main) {
                        if (!BaseActivity.isActivityPause) {
                            if (mInterstitialAdSplash != null) {
                                mInterstitialAdSplash?.show(activity)
                                mInterstitialAdSplash?.fullScreenContentCallback =
                                    object : FullScreenContentCallback() {
                                        override fun onAdImpression() {
                                            super.onAdImpression()
                                            Log.i(TAG, "Ad impression")
                                            isShowing = true
                                            mInterstitialAdSplash = null
                                            LoadingDialog.hideLoadingDialog()
                                            dismissCallback("")
                                        }

                                        override fun onAdDismissedFullScreenContent() {
                                            super.onAdDismissedFullScreenContent()
                                            Log.i(TAG, "Ad Dismiss")
                                            BaseActivity.interstitialShown = false
                                            adCappingBetweenInterstitialAndAppOpenAd =
                                                Calendar.getInstance().timeInMillis
                                            interstitialTimeElapsed =
                                                Calendar.getInstance().timeInMillis
                                            isShowing = false

                                            val loadDelay: Long =
                                                (Constants.INTERSTITIAL_AD_CAPPING - 8L) * 1000L
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                if (!activity.isFinishing && !activity.isDestroyed) {
                                                    loadInterstitialAd(activity)
                                                }
                                            }, loadDelay)

                                            LoadingDialog.hideLoadingDialog()
                                        }

                                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                            super.onAdFailedToShowFullScreenContent(p0)
                                            isShowing = false
                                            dismissCallback("failedToShow")
                                        }
                                    }
                            }
                            LoadingDialog.hideLoadingDialog()
                        } else {
                            LoadingDialog.hideLoadingDialog()
                        }
                    }
                }
            } else {
                dismissCallback.invoke("")
                BaseActivity.interstitialShown = false
                LoadingDialog.hideLoadingDialog()
            }
        } else {
            dismissCallback.invoke("")
        }
    }

    fun loadSplashInterstitialAd(activity: Activity) {
        if (!BillingUtilsIAP.isPremium && mInterstitialAdSplash == null) {
            Log.i(TAG, "Ad Call with ID: ${activity.resources.getString(R.string.splash_interstitial_id)}")
            isAdLoading = true
            InterstitialAd.load(
                activity,
                activity.resources.getString(R.string.splash_interstitial_id),
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        BaseActivity.interstitialShown = false
                        Log.d(TAG, adError.message)
                        mInterstitialAdSplash = null
                        isAdLoading = false
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        Log.i(TAG, "Ad Loaded")
                        mInterstitialAdSplash = interstitialAd
                        isAdLoading = false
                    }
                })
        }
    }

    fun isSplashLoaded(): Boolean {
        return mInterstitialAdSplash != null
    }

    /**
     * Other Functions
     */

    private fun timeDifference(millis: Long): Int {
        val current = Calendar.getInstance().timeInMillis
        val elapsedTime = current - millis

        return TimeUnit.MILLISECONDS.toSeconds(elapsedTime).toInt()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val nwInfo = connectivityManager.activeNetworkInfo ?: return false
            return nwInfo.isConnected
        }
    }

}