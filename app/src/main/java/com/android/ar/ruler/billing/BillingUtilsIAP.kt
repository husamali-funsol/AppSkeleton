package com.android.ar.ruler.billing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.android.ar.ruler.R
import com.android.ar.ruler.ui.activities.MainActivity
import com.android.ar.ruler.utils.Constants.Companion.SUBS_KEY
import com.android.ar.ruler.utils.Constants.Companion.USER_PREMIUM
import com.android.ar.ruler.utils.MyPreferences
import com.android.billingclient.api.*
import com.funsol.iap.billing.BillingClientListener
import com.funsol.iap.billing.BillingEventListener
import com.funsol.iap.billing.FunSolBillingHelper
import com.funsol.iap.billing.model.ErrorType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

@SuppressLint("StaticFieldLeak")
object BillingUtilsIAP {

    var billingHelper: FunSolBillingHelper? = null
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    var isPurchased: Boolean = false
    lateinit var pref :MyPreferences
//        get() = billingHelper?.isInAppPremiumUser() == true or (billingHelper?.isSubsPremiumUser() == true)

    //    var isPremium: MutableLiveData<Boolean> = MutableLiveData(false)
    @JvmField
    var isPremium = false

    const val TAG = "billing"

    fun setUpConnection(activity: Activity) {
        Log.i(TAG, "setUpConnection: ")
         pref = MyPreferences()
        isPremium = pref.getBooleanPreferences(activity,USER_PREMIUM)
        billingHelper = FunSolBillingHelper(activity)
            .setSubKeys(mutableListOf(SUBS_KEY))
            .enableLogging()
            .setBillingClientListener(object : BillingClientListener {
                override fun onClientReady() {
                    Log.i("billing", "onClientReady: ")
                    CoroutineScope(Dispatchers.Main).launch {
                        if (billingHelper?.isInAppPremiumUser() == true || billingHelper?.isSubsPremiumUser() == true) {
//                            billingCallBack.invoke(true)
                            Log.i(TAG, "setUpConnection: is Premium")
                            isPurchased = true
                            isPremium = true
                        } else {
//                            billingCallBack.invoke(false)
                            Log.i(TAG, "setUpConnection: is not Premium")
                            isPurchased = false
                            isPremium = false
                        }
                    }
                }

                override fun onClientInitError() {
                    Log.i("billing", "onClientInitError: ")
                }

            })
        attachListeners(activity)
    }

    private fun attachListeners(activity: Activity) {
        billingHelper?.setBillingEventListener(object : BillingEventListener {
            override fun onProductsPurchased(purchases: List<Purchase?>) {
                pref.setBooleanPreferences(activity,USER_PREMIUM, true)
                isPurchased = true
                isPremium = true
                Toast.makeText(activity, activity.getText(R.string.restartingApp), Toast.LENGTH_SHORT).show()
                handler.postDelayed({
                    activity.startActivity(Intent(activity, MainActivity::class.java))
                    activity.overridePendingTransition(0, 0)
                    activity.finishAffinity()
                    exitProcess(0)
                }, 1000)

//                activity.finish()
//                activity.startActivity(Intent(activity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
            }

            override fun onPurchaseAcknowledged(purchase: Purchase) {
                pref.setBooleanPreferences(activity,USER_PREMIUM, true)
                isPurchased = true
                isPremium = true
            }

            override fun onPurchaseConsumed(purchase: Purchase) {
            }

            /*override fun onPurchaseConsumed(purchase: Purchase) {

            }*/

            override fun onBillingError(error: ErrorType) {
                when (error) {
                    ErrorType.CLIENT_NOT_READY -> {
                        Log.d(TAG, "onBillingError: CLIENT_NOT_READY")
                    }

                    ErrorType.CLIENT_DISCONNECTED -> {
                        Log.d(TAG, "onBillingError: CLIENT_DISCONNECTED")
                    }

                    ErrorType.PRODUCT_NOT_EXIST -> {
                        Log.d(TAG, "onBillingError: PRODUCT_NOT_EXIST")
                    }

                    ErrorType.BILLING_ERROR -> {
                        Log.d(TAG, "onBillingError: BILLING_ERROR")
                    }

                    ErrorType.USER_CANCELED -> {
                        Log.d(TAG, "onBillingError: USER_CANCELED")
                    }

                    ErrorType.SERVICE_UNAVAILABLE -> {
                        Log.d(TAG, "onBillingError: SERVICE_UNAVAILABLE")
                    }

                    ErrorType.BILLING_UNAVAILABLE -> {
                        Log.d(TAG, "onBillingError: BILLING_UNAVAILABLE")
                    }

                    ErrorType.ITEM_UNAVAILABLE -> {
                        Log.d(TAG, "onBillingError: ITEM_UNAVAILABLE")
                    }

                    ErrorType.DEVELOPER_ERROR -> {
                        Log.d(TAG, "onBillingError: DEVELOPER_ERROR")
                    }

                    ErrorType.ERROR -> {
                        Log.d(TAG, "onBillingError: ERROR")
                    }

                    ErrorType.ITEM_ALREADY_OWNED -> {
                        Log.d(TAG, "onBillingError: ITEM_ALREADY_OWNED")
                    }

                    ErrorType.ITEM_NOT_OWNED -> {
                        Log.d(TAG, "onBillingError: ITEM_NOT_OWNED")
                    }

                    ErrorType.SERVICE_DISCONNECTED -> {
                        Log.d(TAG, "onBillingError: SERVICE_DISCONNECTED")
                    }

                    ErrorType.ACKNOWLEDGE_ERROR -> {
                        Log.d(TAG, "onBillingError: ACKNOWLEDGE_ERROR")
                    }

                    ErrorType.ACKNOWLEDGE_WARNING -> {
                        Log.d(TAG, "onBillingError: ACKNOWLEDGE_WARNING")
                    }

                    ErrorType.OLD_PURCHASE_TOKEN_NOT_FOUND -> {
                        Log.d(TAG, "onBillingError: OLD_PURCHASE_TOKEN_NOT_FOUND")
                    }

                    else -> {

                    }
                }
            }
        })
    }

    fun isProductPurchased(): Boolean {
        return isPurchased//billingHelper?.isInAppPremiumUser() == true || billingHelper?.isSubsPremiumUser() == true
    }

    fun purchase(activity: Activity) {
        billingHelper?.buyInApp(activity, "lifetime_product", false)
    }

    fun subscribe(activity: Activity, productID:String,offerID: String) {

        billingHelper?.subscribe(activity, productID,offerID)

//         val  list = billingHelper?.getProductPriceByKey("weekly-gold", "base-plan-trial")?.productCompleteInfo?.subscriptionOfferDetails
//        Log.i("fdslkfewr", "subscribe: ${list}")
//        billingHelper?.getProductPriceByKey("weekly-gold", "")?.productCompleteInfo?.subscriptionOfferDetails!!.forEach {
//            Log.i("dferereerrc", "subscribe: ${it}")
//
//            it.pricingPhases.pricingPhaseList.forEach {
//                Log.i("dsklfjsferer", "subscribe: ${it.billingPeriod}")
//            }
//        }
    }

//    fun getAmountLifetime(activity: Activity): String {
//        return billingHelper?.getProductPriceByKey("lifetime_product")?.price ?: ""
//    }

    fun getAmountMonthly(activity: Activity): String {
        return billingHelper?.getProductPriceByKey("monthly-subscription", "gold-month-trail")?.price ?: ""
    }

    fun getAmountAnnually(activity: Activity): String {
        return billingHelper?.getProductPriceByKey("annual-subscription", "gold-year-trail")?.price ?: ""
    }

    fun getWeeklyPrice(): String {
        return billingHelper?.getProductPriceByKey("weekly-subscription", "gold-week-trail")?.price ?: ""
    }
}