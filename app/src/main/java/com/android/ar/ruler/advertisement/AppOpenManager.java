package com.android.ar.ruler.advertisement;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;
import com.android.ar.ruler.R;
import com.android.ar.ruler.billing.BillingUtilsIAP;
import com.android.ar.ruler.ui.activities.BaseActivity;
import com.android.ar.ruler.ui.activities.MyApp;
import com.android.ar.ruler.utils.Constants;
import com.google.android.gms.ads.AdActivity;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

//*Prefetches App Open Ads.

public class AppOpenManager implements LifecycleObserver, Application.ActivityLifecycleCallbacks {
    private static final String LOG_TAG = "appopen_ad_log";
    private static final String AD_UNIT_ID =/* BuildConfig.DEBUG ?*/ "ca-app-pub-3940256099942544/3419835294"/* : ""*/;
    //private static final String AD_UNIT_ID = BuildConfig.DEBUG ? "ca-app-pub-7849136466938181/8430046404" : "ca-app-pub-7849136466938181/8430046404";
    private AppOpenAd appOpenAd = null;

    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private long interstitialTimeElapsed = 0L;
    private final MyApp myApplication;
    private Activity currentActivity;

    private ConstraintLayout constraintLayout = null;
    private View blackView = null;

     static boolean isShowingAd = false;
    private static boolean dontshow = false;

    private long loadTime = 0; // to keep track of time because ad expires 4 hours after loading

//*Constructor

    public AppOpenManager(MyApp myApplication) {
        this.myApplication = myApplication;
        this.myApplication.registerActivityLifecycleCallbacks(this);//register interface for current activity to listen to all current activity events.
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this); //listen for foregrounding events in your
    }

//*LifecycleObserver methods

    @OnLifecycleEvent(ON_START)
    public void onStart() {
        if (!BillingUtilsIAP.isPremium) {
            showAdIfAvailable();
        }
        Log.d(LOG_TAG, "onStart");
    }

//*Request an ad

    public void fetchAd() {
        if (!BillingUtilsIAP.isPremium) {
            // Have unused ad, no need to fetch another.
            if (isAdAvailable()) {
                return;
            }
            loadCallback =
                    new AppOpenAd.AppOpenAdLoadCallback() {
                        //**Called when an app open ad has loaded.**@param ad the loaded app open ad.
                        @Override
                        public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                            Log.i(LOG_TAG, "Ad Loaded");
                            AppOpenManager.this.appOpenAd = appOpenAd;
                            AppOpenManager.this.loadTime = (new Date()).getTime();
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            super.onAdFailedToLoad(loadAdError);
                        }
                    };
            try {
                Log.i(LOG_TAG, "Ad Call with ID: "+AD_UNIT_ID);
                AdRequest request = getAdRequest();
                AppOpenAd.load(
                        myApplication, AD_UNIT_ID, request,
                        AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
            } catch (Exception e) {
                Log.i(LOG_TAG, "fetchAd: $e");
            }
        }
    }


//*Utility method to check if ad was loaded more than n hours ago.

    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

//*Utility method that checks if ad exists and can be shown.

    public boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }


//*Shows the ad if one isn't already showing.

    public void showAdIfAvailable() {
        if (!BillingUtilsIAP.isPremium) {
            if (!isShowingAd
                    && isAdAvailable()
                    && Constants.APP_OPEN_AD_STRATEGY == 1
                    && !dontshow
                    && !BaseActivity.interstitialShown
                    && timeDifference(interstitialTimeElapsed) > 20
                    && timeDifference(InterstitialHelper.INSTANCE.getAdCappingBetweenInterstitialAndAppOpenAd()) > 10
                    && !BaseActivity.doNotShowAppOpenAd) {
                FullScreenContentCallback fullScreenContentCallback =
                        new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Set the reference to null so isAdAvailable() returns false.
                                Log.i(LOG_TAG, "Ad Dismiss");
                                BaseActivity.interstitialShown = false;
                                interstitialTimeElapsed =
                                        Calendar.getInstance().getTimeInMillis();
                                InterstitialHelper.INSTANCE.setAdCappingBetweenInterstitialAndAppOpenAd(Calendar.getInstance().getTimeInMillis());
                                try {
                                    if (blackView != null || constraintLayout != null) {
                                        constraintLayout.removeView(blackView);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                AppOpenManager.this.appOpenAd = null;
                                isShowingAd = false;
                                fetchAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                BaseActivity.interstitialShown = false;
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                Log.i(LOG_TAG, "Ad Impression");
                                isShowingAd = true;
                                BaseActivity.interstitialShown = true;
                                try {
                                    blackView = new View(currentActivity);
                                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                                    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                                    ViewCompat.setElevation(blackView, 50f);
                                    blackView.setElevation(50f);
                                    blackView.setLayoutParams(lp);
                                    blackView.setBackgroundColor(Color.BLACK);
                                    blackView.invalidate();
                                    constraintLayout = (currentActivity).findViewById(R.id.main_layout);
                                    if (constraintLayout != null && blackView != null) {
                                        constraintLayout.addView(blackView);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        };

                appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
                appOpenAd.show(currentActivity);

            } else {
                fetchAd();
            }
        }
    }

    private int timeDifference(Long millis){
        long current = Calendar.getInstance().getTimeInMillis();
        long elapsedTime = current - millis;

        return Integer.parseInt(String.valueOf(TimeUnit.MILLISECONDS.toSeconds(elapsedTime)));
    }

//*Creates and returns ad request .

    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }


//*Utility method that checks if ad exists and can be shown.

/*    public boolean isAdAvailable() {
        return appOpenAd != null;

    }*/


    //track of current activity
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        //currentActivity = activity;

        if (!(activity instanceof AdActivity))
            currentActivity = activity;
//        if (activity instanceof SplashFragment) {
//           dontshow = true;
//        } else {
//            dontshow = false;
//        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
//        currentActivity = activity;

        if (!(activity instanceof AdActivity))
            currentActivity = activity;

//       if (activity instanceof SplashActivity) {
//            dontshow = true;
//       } else {
//            dontshow = false;
//        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (!(activity instanceof AdActivity))
            currentActivity = activity;
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (!(activity instanceof AdActivity))
            currentActivity = activity;
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        if (!(activity instanceof AdActivity))
            currentActivity = activity;
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        currentActivity = null;
    }
}
