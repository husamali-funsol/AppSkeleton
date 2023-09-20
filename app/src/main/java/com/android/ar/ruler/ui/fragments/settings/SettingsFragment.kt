package com.android.ar.ruler.ui.fragments.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.ar.ruler.R
import com.android.ar.ruler.advertisement.NativeHelper
import com.android.ar.ruler.databinding.FragmentSettingsBinding
import com.android.ar.ruler.ui.activities.MainActivity
import com.android.ar.ruler.ui.fragments.BaseFragment
import com.android.ar.ruler.ui.fragments.SharedViewModel
import kotlinx.coroutines.launch

class SettingsFragment : BaseFragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val settingViewModel: SettingsViewModel by viewModels()

    private val homeSharedViewModel: SharedViewModel by activityViewModels() //using androidx

    /*** Overrides */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        adjustFontScale(resources.configuration)

        nativeAd()

        observers()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*** Functions */

    private fun observers(){
        // Start a coroutine in the lifecycle scope
        viewLifecycleOwner.lifecycleScope.launch {
            // repeatOnLifecycle launches the block in a new coroutine every time the
            // lifecycle is in the STARTED state (or above) and cancels it when it's STOPPED.
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Trigger the flow and start listening for values.
                // Note that this happens when lifecycle is STARTED and stops
                // collecting when the lifecycle is STOPPED
                settingViewModel.text.collect { value ->
                    // New value received
                    binding.txtSetting.text = value
                }
            }
        }

        homeSharedViewModel.message.observe(viewLifecycleOwner){
            binding.txtSettingSharedData.text = it
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
                        400,
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
                                            400
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
    }

}