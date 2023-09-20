package com.android.ar.ruler.ui.fragments.language

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.android.ar.ruler.R
import com.android.ar.ruler.adapters.AdapterForLanguage
import com.android.ar.ruler.advertisement.InterstitialHelper
import com.android.ar.ruler.advertisement.NativeHelper
import com.android.ar.ruler.billing.BillingUtilsIAP
import com.android.ar.ruler.databinding.FragmentLanguageBinding
import com.android.ar.ruler.models.LanguagesModel
import com.android.ar.ruler.ui.activities.MainActivity
import com.android.ar.ruler.ui.fragments.BaseFragment
import com.android.ar.ruler.utils.Constants
import com.android.ar.ruler.utils.Constants.Companion.CHANGE_LANGUAGE
import com.android.ar.ruler.utils.Constants.Companion.LANG_NAME
import com.android.ar.ruler.utils.Constants.Companion.SHOW_LANGUAGE_SCREEN
import com.android.ar.ruler.utils.Extensions.canShow
import com.android.ar.ruler.utils.MyPreferences
import com.android.ar.ruler.utils.languages
import java.util.Locale

class LanguageFragment : BaseFragment() {

    private var pref: MyPreferences? = null
    private lateinit var adapter: AdapterForLanguage
    private val myList = ArrayList<LanguagesModel>()

    private var _binding: FragmentLanguageBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /*** Overrides */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            if (it is MainActivity) {
                postAnalytic("language_fragment_onCreate")
                postFragNameAnalytic("language_fragment")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment

        adjustFontScale(resources.configuration)

        nativeAd()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let { mContext ->
            if (findNavController().previousBackStackEntry?.destination?.id == null) {
                binding.ivBack.canShow(false)
            }

            pref?.getBooleanPreferences(mContext, Constants.NIGHT_MODE)?.let { setSystemBarTheme(requireActivity(), it) }
            activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.toolBarColor)

            binding.tvLanguage.text = pref?.getStringPreferences(mContext, LANG_NAME)
            val sharedPreferences = context?.getSharedPreferences("prefs", AppCompatActivity.MODE_PRIVATE)
            myList.clear()
            languages.forEach { languagesModel ->
                if (sharedPreferences?.getString(
                        CHANGE_LANGUAGE,
                        Locale.getDefault().language
                    ) != languagesModel.code
                ) {
                    myList.add(languagesModel)
                } else {
                    binding.tvLanguage.text = languagesModel.name
                }
            }
            sharedPreferences?.let {
                adapter = AdapterForLanguage(it, myList) { item, Position ->
                    binding.selectedImage.setImageResource(R.drawable.ic_unselect)
                }
            }
            binding.recycler.adapter = adapter

            binding.tvLanguage.setOnClickListener {
                binding.selectedImage.setImageResource(R.drawable.ic_select)
                adapter.isSelected = -1
                adapter.notifyDataSetChanged()
            }

            binding.selectedImage.setOnClickListener {
                binding.tvLanguage.performClick()
            }

            binding.btnApply.setOnClickListener {
                if (adapter.isSelected != -1) {
                    pref?.setStringPreferences(
                        mContext,
                        LANG_NAME,
                        adapter.getList()[adapter.isSelected].name
                    )
                    sharedPreferences?.edit()?.putString(CHANGE_LANGUAGE, adapter.getList()[adapter.isSelected].code)?.apply()
                    if (pref?.getBooleanPreferences(mContext, SHOW_LANGUAGE_SCREEN) == true) {
                        pref?.setBooleanPreferences(mContext, SHOW_LANGUAGE_SCREEN, false)

                        val language: String? = sharedPreferences?.getString(
                            CHANGE_LANGUAGE,
                            Locale.getDefault().language
                        )
                        language?.let {
                            setLocale(it)
                        }
                        openMainFragment()
                    } else {
                        val language: String? = sharedPreferences?.getString(
                            CHANGE_LANGUAGE,
                            Locale.getDefault().language
                        )
                        language?.let {
                            setLocale(it)
                        }
                        openMainFragment()
                    }
                } else {
                    if (pref?.getBooleanPreferences(mContext, SHOW_LANGUAGE_SCREEN) == true) {
                        pref?.setBooleanPreferences(mContext, SHOW_LANGUAGE_SCREEN, false)
                        val language: String? = sharedPreferences?.getString(
                            CHANGE_LANGUAGE,
                            Locale.getDefault().language
                        )
                        language?.let {
                            setLocale(it)
                        }
                        openMainFragment()
                    } else if (findNavController().previousBackStackEntry?.destination?.id == R.id.nav_homeFragment) {
                        findNavController().popBackStack()
                    } else {
                        val language: String? = sharedPreferences?.getString(
                            CHANGE_LANGUAGE,
                            Locale.getDefault().language
                        )
                        language?.let {
                            setLocale(it)
                        }
                        openMainFragment()
                    }
                }
            }

            binding.ivBack.setOnClickListener {
                findNavController().popBackStack()
            }

            activity?.onBackPressedDispatcher?.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (findNavController().previousBackStackEntry?.destination?.id == R.id.nav_homeFragment) {
                            findNavController().popBackStack()
                        }
                    }
                })
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            if (it is MainActivity)
                postAnalytic("language_fragment_onResume")
        }
    }

    /*** Functions */

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val resources = resources
        val configuration: Configuration = resources.configuration
        configuration.setLocale(locale)

        val appContext = requireContext().applicationContext
        val appResources = appContext.resources
        val appConfiguration: Configuration = appResources.configuration
        appConfiguration.setLocale(locale)

        resources.updateConfiguration(configuration, resources.displayMetrics)
        appResources.updateConfiguration(appConfiguration, appResources.displayMetrics)

        // Set layout direction based on selected language
        if (TextUtils.getLayoutDirectionFromLocale(locale) == ViewCompat.LAYOUT_DIRECTION_RTL) {
            // Set layout direction to right-to-left
            activity?.window?.decorView?.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            // Set layout direction to left-to-right
            activity?.window?.decorView?.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
    }

    private fun openMainFragment() {
        if (findNavController().currentDestination?.id == R.id.languageFragment) {
            navigateToFragment(R.id.nav_homeFragment)
            activity?.let {
                it.recreate()
            }
        }
    }

    private fun setSystemBarTheme(pActivity: Activity, pIsDark: Boolean) {
        // Fetch the current flags.
        val lFlags = pActivity.window.decorView.systemUiVisibility
        // Update the SystemUiVisibility dependening on whether we want a Light or Dark theme.
        pActivity.window.decorView.systemUiVisibility =
            if (pIsDark) lFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() else lFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    /*** AdMob Ads */

    private fun nativeAd() {
        if (Constants.SHOW_ONBOARDING_NATIVE_AD == 1) {
                if (context?.let { isNetworkAvailable(it) } == true) {
                    binding.nativeContainer.visibility = View.VISIBLE
                    activity?.let { fragmentActivity ->
                        if (fragmentActivity is MainActivity) {
                            NativeHelper.loadAdsWithConfiguration(
                                fragmentActivity,
                                binding.nativeContainer,
                                binding.admobContainer,
                                500,
                                fragmentActivity.getString(R.string.home_native_id),
                                null
                            ) { isAdLoaded ->
                                if (isAdLoaded) {
                                    activity?.let {
                                        if (it is MainActivity) {
                                            if (isVisible && NativeHelper.adMobNativeAd != null) {
                                                NativeHelper.populateUnifiedNativeAdView(
                                                    it,
                                                    NativeHelper.adMobNativeAd,
                                                    binding.nativeContainer,
                                                    binding.admobContainer,
                                                    500
                                                ) { adStatus ->
                                                    Log.d("MyNativeAdTag", "Ad impression: $adStatus")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    binding.nativeContainer.visibility = View.GONE
                }
        } else {
            binding.nativeContainer.canShow(false)
        }
    }

}