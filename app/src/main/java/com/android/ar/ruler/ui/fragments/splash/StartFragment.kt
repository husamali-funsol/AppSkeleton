package com.android.ar.ruler.ui.fragments.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.android.ar.ruler.R
import com.android.ar.ruler.advertisement.InterstitialHelper
import com.android.ar.ruler.advertisement.NativeHelper
import com.android.ar.ruler.advertisement.NativeHelper.adMobNativeAd
import com.android.ar.ruler.billing.BillingUtilsIAP
import com.android.ar.ruler.databinding.FragmentStartBinding
import com.android.ar.ruler.ui.activities.BaseActivity.Companion.doNotShowAppOpenAd
import com.android.ar.ruler.ui.activities.MainActivity
import com.android.ar.ruler.ui.fragments.BaseFragment
import com.android.ar.ruler.utils.Constants
import com.android.ar.ruler.utils.Extensions.setOnOneClickListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * First [Fragment] of navigation graph.
 * Named Start fragment instead of Splash to avoid play console warning
 */
//@AndroidEntryPoint
class StartFragment : BaseFragment() {

//    Note: If you want to use binding with dagger hilt uncomment these 2 lines and @AndroidEntryPoint
//    @Inject
//    lateinit var binding: FragmentStartBinding

    private var _binding: FragmentStartBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var pauseProgress: Int = 0
    private var isPaused = false
    private var maxProgress = 0
    private var nativeAdImpression = false

    companion object {}


    /*** Overrides */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            if (it is MainActivity) {
                postAnalytic("splash_fragment_onCreate")
                postFragNameAnalytic("splash_fragment")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStartBinding.inflate(inflater, container, false)
        val root: View = binding.root

        adjustFontScale(resources.configuration)

        activity?.let { forceUpdate(it) }

        init()

        loadInterstitialAd()

        listeners()

        nativeAd()

        backPressCallback()

        return root

    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        maxProgress =
            if (!BillingUtilsIAP.isPremium && context?.let { isNetworkAvailable(it) } == true) 1000 else 200
        binding.pbStart.progress = 0
        binding.pbStart.max = maxProgress
        handler = Handler(Looper.getMainLooper())
        binding.tvLetsStart.text = getString(R.string.app_name)
        runnable = Runnable {
            binding.pbStart.progress = binding.pbStart.progress.plus(1)
            if (!BillingUtilsIAP.isPremium && InterstitialHelper.isSplashLoaded() && nativeAdImpression) {
                binding.pbStart.progress = maxProgress
            }
            binding.tvLetsStart.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.tvLetsStart.text =
                ((binding.pbStart.progress * 100) / maxProgress).toString() + "%"
            if (binding.pbStart.progress < maxProgress) {
                runnable.let { handler.postDelayed(it, 10) }
            } else if (binding.pbStart.progress == maxProgress) {
                binding.tvLetsStart.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                binding.tvLetsStart.text = getString(R.string.let_s_start)
                try {
                    val shake: Animation =
                        AnimationUtils.loadAnimation(requireContext(), R.anim.shake)
                    binding.pbStart.startAnimation(shake)
                } catch (_: Exception) {
                }
                if (binding.pbStart.progress == maxProgress) {
                    binding.tvLetsStart.setOnClickListener {
                        showADThenMoveNext()
                    }
                }
            } else if (binding.pbStart.progress == maxProgress) {
                binding.tvLetsStart.text = "Continue"
                if (binding.pbStart.progress == maxProgress) {
                    binding.tvLetsStart.setOnClickListener {
                        showADThenMoveNext()
                    }
                }
            }
        }
        handler.postDelayed(runnable, 1)

    }


    override fun onResume() {
        super.onResume()
        if (isPaused) {
            binding.pbStart.progress = pauseProgress
            handler.postDelayed(runnable, 1)
        }
    }

    override fun onPause() {
        super.onPause()
        isPaused = true

        pauseProgress = binding.pbStart.progress
        runnable.let { handler.removeCallbacks(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        doNotShowAppOpenAd = false
        nativeAdImpression = false
    }

    /*** Functions */

    private fun init() {
        doNotShowAppOpenAd = true
    }

    private fun loadInterstitialAd() {
        if (!BillingUtilsIAP.isPremium && context?.let { isNetworkAvailable(it) } == true) {
            activity?.let {
                if (it is MainActivity) {
                    InterstitialHelper.loadSplashInterstitialAd(it)
                }
            }
        }
    }

    private fun backPressCallback() {
        configureBackPress { }
    }

    private fun listeners() {
        binding.tvLetsStart.setOnOneClickListener {
            if (binding.pbStart.progress == maxProgress) {
                postAnalytic("splash_fragment_btn_lets_start_clicked")
                showADThenMoveNext()
            }
        }
    }

    private fun showADThenMoveNext() {
        activity?.let {
            InterstitialHelper.showSplashInterstitial(it) {
                //How to navigate and send data to another fragment with bundle
                if (Constants.SHOW_ONBOARDING_SCREEN == 1) {
                    if (findNavController().currentDestination?.id == R.id.nav_startFragment) {
                        val navOptions = NavOptions.Builder()
                            .setEnterAnim(android.R.anim.fade_in)
                            .setExitAnim(android.R.anim.fade_out)
                            .setPopEnterAnim(android.R.anim.fade_in)
                            .setPopExitAnim(android.R.anim.fade_out)
                            .setPopUpTo(R.id.nav_startFragment, true)
                            .build()
                        navigateToFragment(R.id.languageFragment, navOptions = navOptions)
                    }
                } else {
                    if (findNavController().currentDestination?.id == R.id.nav_startFragment) {
                        val bundle: Bundle = bundleOf()
                        bundle.putBoolean(
                            Constants.IS_PIN_CREATED,
                            true
                        )
                        navigateToFragment(R.id.nav_homeFragment, bundle = bundle)
                    }
                }
            }
        }
    }

    /*** AdMob Ads */

    private fun nativeAd() {
        if (context?.let { isNetworkAvailable(it) } == true) {
            binding.nativeContainer.visibility = View.VISIBLE
            activity?.let { fragmentActivity ->
                if (fragmentActivity is MainActivity) {
                    NativeHelper.loadAdsWithConfiguration(
                        fragmentActivity,
                        binding.nativeContainer,
                        binding.admobContainer,
                        100,
                        fragmentActivity.getString(R.string.home_native_id),
                        null
                    ) { isAdLoaded ->
                        if (isAdLoaded) {
                            activity?.let {
                                if (it is MainActivity) {
                                    if (isVisible && adMobNativeAd != null) {
                                        NativeHelper.populateUnifiedNativeAdView(
                                            it,
                                            adMobNativeAd,
                                            binding.nativeContainer,
                                            binding.admobContainer,
                                            100
                                        ) { adStatus ->
                                            nativeAdImpression = true
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
    }

}