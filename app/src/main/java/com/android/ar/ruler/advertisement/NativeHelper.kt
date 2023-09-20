package com.android.ar.ruler.advertisement

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.ar.ruler.R
import com.android.ar.ruler.billing.BillingUtilsIAP
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

object NativeHelper {
    private const val TAG = "native_ad_log"
    private lateinit var runnable: Runnable
    private val handler = Handler(Looper.getMainLooper())
    fun loadAdsWithConfiguration(
         context: Activity,
        nativeContainer: ConstraintLayout,
        adMobContainer: FrameLayout,  /* NativeAdLayout fbContainer,*/
        height: Int,
        adMobId: String,
        fbAdId: String?,
        config: Int = 1,
        adClosePressed: (Boolean) -> Unit
    ) {
        if (!BillingUtilsIAP.isPremium) {
        if (config.toDouble() == 0.0) { //No Ads
            nativeContainer.visibility = View.GONE
        } else if (config.toDouble() == 1.0 || config.toDouble() == 3.0) { //1 = AdMob Only, 3 = AdMob -> Facebook
            loadAdMob(
                context,
                nativeContainer,
                adMobContainer,  /*fbContainer,*/
                height,
                adMobId,
                fbAdId,
                config,
                adClosePressed
            )
        }
        } else {
            nativeContainer.visibility = View.GONE
        }

    }

    private fun showNative(
        context: Activity,
        nativeContainer: ConstraintLayout,
        adMobContainer: FrameLayout,  /*NativeAdLayout fbContainer,*/
        height: Int,
        adMobId: String,
        fbAdId: String?,
        config: Int,
        adClosePressed: (Boolean) -> Unit
    ) {
        if (!isNativeLoading) {
            if (::runnable.isInitialized){
                handler.removeCallbacks(runnable)
            }
            isNativeLoading = true
            if (adMobNativeAd == null) {
                Log.i(TAG, "Ad Call with ID: $adMobId")
                val builder: AdLoader.Builder = AdLoader.Builder(context.applicationContext, adMobId)
                val adLoader = builder.forNativeAd { unifiedNativeAd: NativeAd? ->
                    adMobNativeAd = unifiedNativeAd
                }.withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(i: LoadAdError) {
                        super.onAdFailedToLoad(i)
                        isNativeLoading = false
                        Log.i(TAG, "Failed; code: " + i.code + ", message: " + i.message)
                        nativeContainer.visibility = View.GONE
                        adClosePressed(false)
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Log.i(TAG, "Ad Loaded")
                        isNativeLoading = false
                        nativeContainer.visibility = View.VISIBLE
                        adMobContainer.visibility = View.VISIBLE
                        adClosePressed(true)
                    }
                }).withNativeAdOptions(
                    com.google.android.gms.ads.nativead.NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT).build()
                ).build()
                adLoader.loadAd(AdRequest.Builder().build())
            } else {
                nativeContainer.visibility = View.VISIBLE
                adMobContainer.visibility = View.VISIBLE
                adClosePressed(true)
            }
        } else {
            if (::runnable.isInitialized){
                handler.removeCallbacks(runnable)
            }
            runnable = Runnable {
                if (isNativeLoading && adMobNativeAd ==null) {
                    runnable.let { handler.postDelayed(it, 10) }
                } else {
                    adClosePressed(true)
                }
            }
            handler.postDelayed(runnable, 10)
        }
    }

    private fun loadAdMob(
        context: Activity,
        nativeContainer: ConstraintLayout,
        adMobContainer: FrameLayout,  /*NativeAdLayout fbContainer,*/
        height: Int,
        adMobId: String,
        fbAdId: String?,
        config: Int,
        adClosePressed: (Boolean) -> Unit
    ) {
        if (!BillingUtilsIAP.isPremium) {
        if (height == -1) {
            //Check the Height
            val vto = nativeContainer.viewTreeObserver
            vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val heightPx = nativeContainer.measuredHeight
                    val heightDp = (heightPx / context.resources.displayMetrics.density).toInt()
                    nativeContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    if (heightDp < 49) {
                        nativeContainer.visibility = View.GONE
                    } else {
                        showNative(context,nativeContainer, adMobContainer,  /* fbContainer, */heightDp, adMobId, fbAdId, config, adClosePressed)
                    }
                }
            })
        } else {
            if (height < 49) {
                nativeContainer.visibility = View.GONE
            } else {
                showNative(context,nativeContainer, adMobContainer,  /* fbContainer, */height, adMobId, fbAdId, config, adClosePressed)
            }
        }
        } else {
            nativeContainer.visibility = View.GONE
        }
    }

    @SuppressLint("InflateParams")
    fun populateUnifiedNativeAdView(
        context: Activity,
        nativeAd: NativeAd?,
        nativeContainer: ConstraintLayout,
        adMobNativeContainer: FrameLayout,
        height: Int,
        adClosePressed: (Boolean) -> Unit
    ) {
        if (nativeAd != null) {
            val textView = nativeContainer.findViewById<TextView>(R.id.loading_ad)
            if (textView != null) textView.visibility = View.GONE
            val inflater = LayoutInflater.from(context)

            val adView: NativeAdView = when (height) {
                in 10..100 -> {
                    inflater.inflate(R.layout.native_7a_design, null) as NativeAdView
                }
                in 101..200 -> {
                    inflater.inflate(R.layout.native_7b_design, null) as NativeAdView
                }
                in 201..300 -> {
                    inflater.inflate(R.layout.native_6a_design, null) as NativeAdView
                }
                in 301..400 -> {
                    inflater.inflate(R.layout.native_6b_design, null) as NativeAdView
                }
                in 401..500 -> {
                    inflater.inflate(R.layout.native_full_button_up, null) as NativeAdView
                }
                in 501..600 -> {
                    inflater.inflate(R.layout.native_full_button_down, null) as NativeAdView
                }
                else -> {
                    inflater.inflate(R.layout.admob_orignal_native, null) as NativeAdView
                }
            }

            adClosePressed(true)

            adMobNativeContainer.visibility = View.VISIBLE
            adMobNativeContainer.removeAllViews()
            adMobNativeContainer.addView(adView)
            if (adView.findViewById<MediaView>(R.id.ad_media) != null) {
                val mediaView: MediaView = adView.findViewById(R.id.ad_media)
                adView.mediaView = mediaView
            }

            // Set other ad assets.
            adView.headlineView = adView.findViewById(R.id.ad_headline)
            adView.bodyView = adView.findViewById(R.id.ad_body)
            adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
            adView.iconView = adView.findViewById(R.id.ad_app_icon)

            //Headline
            if (adView.headlineView != null) {
                if (nativeAd.headline == null) {
                    (adView.headlineView as TextView).visibility = View.INVISIBLE
                } else {
                    (adView.headlineView as TextView).text = nativeAd.headline
                    (adView.headlineView as TextView).isSelected = true
                }
            }


            //Body
            if (adView.bodyView != null) {
                if (nativeAd.body == null) {
                    adView.bodyView?.visibility = View.INVISIBLE
                } else {
                    adView.bodyView?.visibility = View.VISIBLE
                    (adView.bodyView as TextView).text = nativeAd.body
                }
            }

            //Call to Action
            if (adView.callToActionView != null) {
                if (nativeAd.callToAction == null) {
                    adView.callToActionView?.visibility = View.GONE
                    //if you use image filter view then hide that view with CTA
                    adView.findViewById<ImageFilterView>(R.id.call_to_action_bg).visibility = View.GONE
                } else {
                    adView.callToActionView?.visibility = View.VISIBLE
                    (adView.callToActionView as TextView).text = nativeAd.callToAction
                }
            }

            //Icon
            if (adView.iconView != null) {
                if (nativeAd.icon == null) {
                    adView.iconView?.visibility = View.GONE
                } else {
                    (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
                    adView.iconView?.visibility = View.VISIBLE
                }
            }

            //price
            if (adView.priceView != null) {
                if (nativeAd.price == null) {
                    adView.priceView?.visibility = View.GONE
                } else {
                    adView.priceView?.visibility = View.GONE
                    (adView.priceView as TextView).text = nativeAd.price
                }
            }

            //Store
            if (adView.storeView != null) {
                if (nativeAd.store == null) {
                    adView.storeView?.visibility = View.GONE
                } else {
                    adView.storeView?.visibility = View.GONE
                    (adView.storeView as TextView).text = nativeAd.store
                }
            }

            //Rating
            if (adView.starRatingView != null) {
                if (nativeAd.starRating != null) {
                    (adView.starRatingView as RatingBar).rating = nativeAd.starRating.toString().toFloat()
                } else {
                    (adView.starRatingView as RatingBar).visibility = View.INVISIBLE
                }
                adView.starRatingView?.visibility = View.GONE
            }

            //Advertiser
            if (adView.advertiserView != null) {
                if (nativeAd.advertiser != null) {
                    (adView.advertiserView as TextView).text = nativeAd.advertiser
                } else {
                    (adView.advertiserView as TextView).visibility = View.INVISIBLE
                }
                adView.advertiserView?.visibility = View.GONE
            }
            Log.i(TAG, "Ad impression")
            adView.setNativeAd(nativeAd)
        } else {
            nativeContainer.visibility = View.GONE
        }
        isNativeLoading = false
        adMobNativeAd = null
    }
        //---------------------------------------           AdMOB      ---------------------------------------------//
        var adMobNativeAd: NativeAd? = null
        var isNativeLoading = false
}