package com.android.ar.ruler.ui.activities

import android.content.Context
import android.content.IntentSender
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.android.ar.ruler.R
import com.android.ar.ruler.advertisement.InterstitialHelper
import com.android.ar.ruler.databinding.ActivityMainBinding
import com.android.ar.ruler.utils.Constants
import com.android.ar.ruler.utils.Extensions.gone
import com.android.ar.ruler.utils.Extensions.visible
import com.android.ar.ruler.utils.MyContextWrapper
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import java.util.Locale

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    private var navController: NavController? = null

//     private val adapter by lazy{
//        RecyclerViewWithDiffUtils()
//    }

    /*** Overrides */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // uncomment after adding json file from firebase
        // remoteConfig(this@MainActivity){}

        init()

        listeners()

    }

    /**
     * Overrides the base context to enable language switching at runtime.
     *
     * This method is used to modify the base context of the application to enable runtime language switching.
     * It retrieves the preferred language from shared preferences, or uses the device's default language if not specified.
     * The method wraps the context with a custom context wrapper that applies the specified language configuration.
     * This allows the application to dynamically change its language setting without restarting the app.
     *
     * @param context The original context before language wrapping.
     */
    override fun attachBaseContext(context: Context) {
        val sharedPreferences = context.getSharedPreferences("prefs", MODE_PRIVATE)
        val language: String = sharedPreferences.getString(Constants.CHANGE_LANGUAGE, Locale.getDefault().language)!!
        super.attachBaseContext(MyContextWrapper.wrap(context, language))
        val locale = Locale(language)
        val resources: Resources = baseContext.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    /***Functions */

    private fun init() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
        navController?.let { controller ->
            NavigationUI.setupWithNavController(
                binding.bottomNavigationView,
                controller
            )
        }
    }

    private fun listeners() {
        navController?.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.nav_startFragment) {
                binding.bottomNavigationView.gone()
            } else if (destination.id == R.id.nav_homeFragment || destination.id == R.id.nav_infoFragment || destination.id == R.id.nav_settingsFragment) {
                InterstitialHelper.showAndLoadInterstitial(this@MainActivity) {}
                binding.bottomNavigationView.visible()
            }
        }
    }

    /**
     * For RecyclerViewWithDiffUtils
     */

//    private fun setRecyclerViewWithDiffUtilsAdapter(){
//        binding.recyclerView.adapter = adapter
//        binding.recyclerView.layoutManager = LinearLayoutManager(this)
//        adapter.setData(listOf(Person(1,"Shahbaz",33)))
//    }

    /**
     * For Multiple RecyclerViews
     */

//    val initRecycler = {
//        binding.apply {
//            mainLayoutMain.recyclerview.adapter = menuAdapter
//            val layoutManager = mainLayoutMain.recyclerview.layoutManager as GridLayoutManager
//            layoutManager.spanSizeLookup =
//                object : GridLayoutManager.SpanSizeLookup() {
//                    override fun getSpanSize(position: Int): Int {
//                        return when (position) {
//                            0 -> 2
//                            menuAdapter.currentList.size - 1 -> 2
//                            1 -> 2
//                            menuAdapter.currentList.size - 2 -> 2
//                            else -> 1
//                        }
//                    }
//                }
//            menuAdapter.submitList(getData())
//        }
//    }

}