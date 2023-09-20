package com.android.ar.ruler.ui.fragments.info

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.android.ar.ruler.R
import com.android.ar.ruler.advertisement.NativeHelper
import com.android.ar.ruler.databinding.FragmentInfoBinding
import com.android.ar.ruler.ui.activities.MainActivity
import com.android.ar.ruler.ui.fragments.BaseFragment
import com.android.ar.ruler.ui.fragments.SharedViewModel
import com.android.ar.ruler.utils.Constants
import com.android.ar.ruler.utils.Extensions.showToast

class InfoFragment : BaseFragment() {

    private var _binding: FragmentInfoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val infoViewModel: InfoViewModel by viewModels()

    private val homeSharedViewModel: SharedViewModel by activityViewModels() //using androidx


    /*** Overrides */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        adjustFontScale(resources.configuration)

        nativeAd()

        listeners()

        observers()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    /*** Functions */

    private fun listeners(){

    }

    private fun observers(){

        infoViewModel.text.observe(viewLifecycleOwner) {value->
            binding.txtInfo.text = value
        }

        homeSharedViewModel.message.observe(viewLifecycleOwner){
            binding.txtInfoSharedData.text = it
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
                        300,
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
                                            300
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