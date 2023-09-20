package com.android.ar.ruler.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.android.ar.ruler.BuildConfig
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import java.util.Locale

open class BaseFragment : Fragment() {

    private var myFirebaseAnalytics: FirebaseAnalytics? = null
    private lateinit var backPressedCallback: OnBackPressedCallback

    private fun NavController.isFragmentRemovedFromBackStack(destinationId: Int) =
        try {
            getBackStackEntry(destinationId)
            false
        } catch (e: Exception) {
            true
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myFirebaseAnalytics = context?.let { FirebaseAnalytics.getInstance(it) }
    }

    /**
     * Posts an analytic event to Firebase Analytics and displays a short Toast message (in debug mode).
     *
     * This method is used to send custom analytic events to Firebase Analytics. It converts the given
     * event string to lowercase, replaces spaces with underscores, and trims any leading or trailing spaces.
     * The modified event string is then used as the event name when logging the event to Firebase Analytics.
     *
     * @param event The event string to be logged and displayed.
     */
    fun postAnalytic(event: String) {
        // Convert the event string to lowercase, replace spaces with underscores, and trim spaces.
        val fString = event.lowercase(Locale.getDefault()).replace(' ', '_').trim()
        val bundle = Bundle()
        bundle.putString(fString, fString)
        // Log the event to Firebase Analytics with the modified event string as the event name.
        myFirebaseAnalytics?.logEvent(fString, bundle)
        // Display a Toast message if the app is in debug mode.
        if (BuildConfig.DEBUG){
            context?.let {
                Toast.makeText(it, fString, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Logs a screen view analytic event to Firebase Analytics.
     *
     * This method is used to log screen view events to Firebase Analytics, which helps in tracking user
     * navigation within the app. The method takes a screen name as input and logs it as a screen view event.
     * The provided screen name is stored in a Bundle along with the appropriate parameter key, and then
     * this Bundle is used to log the event to Firebase Analytics using the predefined event type
     * `FirebaseAnalytics.Event.SCREEN_VIEW`.
     *
     * @param name The name of the screen being viewed.
     */
    fun postFragNameAnalytic(name: String?) {
        try {
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, name)
            myFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        } catch (_: java.lang.Exception) {
        } catch (_: Exception) {
        }
    }

    /**
     * Configures a custom action to be executed when the back button is pressed.
     *
     * This method allows you to define a custom action that should be executed when the device's
     * back button is pressed. It sets up a callback that intercepts the back button press event
     * and triggers the specified `backPressed` action. This can be used to override the default
     * behavior of the back button within a specific fragment or context.
     *
     * @param backPressed A lambda function representing the custom action to be executed when the back button is pressed.
     */
    fun configureBackPress(backPressed: () -> Unit) {
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backPressed()
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            backPressedCallback
        )
    }

    private val currentNavOptions = NavOptions.Builder()
        .setEnterAnim(android.R.anim.slide_in_left)
        .setExitAnim(android.R.anim.slide_out_right)
        .setPopEnterAnim(android.R.anim.slide_out_right)
        .setPopExitAnim(android.R.anim.slide_in_left).build()

    /**
     * Navigates to a specified fragment using the Navigation component.
     *
     * This method is used to navigate to a desired fragment within the app using the Android Navigation component.
     * It allows for precise control over the navigation process, including handling back stack behavior and providing
     * optional arguments to the destination fragment.
     *
     * @param fragmentId The ID of the destination fragment to navigate to.
     * @param actionId The optional ID of the navigation action to execute. If provided, the action will be used for navigation.
     * @param bundle The optional bundle of arguments to pass to the destination fragment.
     * @param navOptions The optional NavOptions to customize the navigation behavior.
     */
    fun navigateToFragment(
        fragmentId: Int,
        actionId: Int? = null,
        bundle: Bundle = bundleOf(),
        navOptions: NavOptions = currentNavOptions
    ) {
        CoroutineScope(Main).launch {
            // Access the NavController associated with the current fragment.
            findNavController().apply {
                // Check if the current destination is already the desired fragment.
                if (currentDestination?.id != fragmentId) {
                    // Check if the desired fragment is already in the back stack.
                    if (!isFragmentRemovedFromBackStack(fragmentId)) {
                        // Pop the back stack to the desired fragment without reordering.
                        popBackStack(fragmentId, false)
                    } else {
                        try {
                            // Navigate using the provided actionId or directly to the fragmentId with optional bundle and navOptions.
                            if (actionId != null)
                                navigate(actionId, bundle, navOptions)
                            else
                                navigate(fragmentId, bundle, navOptions)
                        } catch (e: Exception) {
                            // If navigation using the actionId fails, navigate directly to the fragmentId with optional bundle and navOptions.
                            navigate(fragmentId, bundle, navOptions)
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks the availability of an active network connection.
     *
     * This method is used to determine if a network connection is available on the device.
     * It checks whether the device is currently connected to a network, and if so, it verifies the type
     * of network (e.g., WiFi, cellular, Ethernet) using the ConnectivityManager and NetworkCapabilities.
     * The method supports both pre- and post-Android Marshmallow (API level 23) devices.
     *
     * @param context The context used to access system services and information.
     * @return `true` if an active network connection is available, `false` otherwise.
     */
    fun isNetworkAvailable(context: Context): Boolean {
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

    /**
     * Initiates a forced app update if an immediate update is available.
     *
     * This method is used to trigger a forced update for the app if an immediate update is available.
     * It checks whether an update is available and whether the update type allowed is IMMEDIATE.
     * If these conditions are met, the method starts the update flow using the AppUpdateManager,
     * forcing the user to update the app.
     *
     * Note: The method is designed to handle the update flow using the Play Store's in-app update API.
     */
    fun forceUpdate(activity: Activity) {
        val appUpdateManager = AppUpdateManagerFactory.create(activity)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(
                    AppUpdateType.IMMEDIATE
                )
            ) {
                try {
                    if (!activity.isFinishing && !activity.isDestroyed) {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.IMMEDIATE, activity, 1000
                        )
                    }
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("quickCast", "forceUpdate: Exception1 : ${e.message}")
                } catch (e: java.lang.Exception) {
                    Log.e("quickCast", "forceUpdate: Exception2 : ${e.message}")
                } catch (e: Exception) {
                    Log.e("quickCast", "forceUpdate: Exception3 : ${e.message}")
                }

            }
        }
    }

    /**
     * Initiates the in-app rating flow using the Play Store's in-app review API.
     *
     * This method is used to prompt users for an in-app rating using the Play Store's in-app review API.
     * It allows the app to request user feedback without navigating away from the app.
     * The method requests the review flow, and if successful, launches the review dialog.
     * If the review flow request fails, the method provides error handling.
     *
     */
    fun showInAppRating() {
        context?.let {
            val manager = ReviewManagerFactory.create(it)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val reviewInfo = result.result
                    val flow = activity?.let { it1 -> manager.launchReviewFlow(it1, reviewInfo) }
                    flow?.addOnCompleteListener { msg ->
                        Log.i("MyRatingTag", "showInAppRating: result -> ${msg.result}")
                        //Continue your application process
                    }
                    flow?.addOnFailureListener { it2 ->
                        Log.i("MyRatingTag", "showInAppRating: ", it2)
                    }
                } else {
                    Log.i("MyRatingTag", "showInAppRating: error")
                    //Handle the error here
                }
            }
        }
    }

    /**
     * Adjusts the font scale of the app's configuration if it's above a certain threshold.
     *
     * This method is used to ensure that the app's font scale remains within acceptable limits.
     * If the provided configuration's font scale exceeds a specified threshold (0.90), the method
     * reduces the font scale to that threshold. This helps in maintaining a consistent and
     * user-friendly font size within the app.
     *
     * @param configuration The Configuration object representing the current app configuration.
     */
    fun adjustFontScale(configuration: Configuration) {
        if (configuration.fontScale > 0.90) {
            //Custom Log class, you can use Log.w
            //Custom Log class, you can use Log.w
            configuration.fontScale = 0.90f
            val metrics = resources.displayMetrics
            val wm = activity?.getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getMetrics(metrics)
            metrics.scaledDensity = configuration.fontScale * metrics.density
            activity?.resources?.updateConfiguration(configuration, metrics)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::backPressedCallback.isInitialized) {
            backPressedCallback.isEnabled = false
            backPressedCallback.remove()
        }
    }
}