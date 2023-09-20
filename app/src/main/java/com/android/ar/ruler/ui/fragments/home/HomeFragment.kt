package com.android.ar.ruler.ui.fragments.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.android.ar.ruler.R
import com.android.ar.ruler.advertisement.NativeHelper
import com.android.ar.ruler.databinding.FragmentHomeBinding
import com.android.ar.ruler.databinding.SheetExitBinding
import com.android.ar.ruler.ui.activities.MainActivity
import com.android.ar.ruler.ui.fragments.BaseFragment
import com.android.ar.ruler.ui.fragments.SharedViewModel
import com.android.ar.ruler.utils.Constants
import com.android.ar.ruler.utils.Constants.Companion.IS_PIN_CREATED
import com.android.ar.ruler.utils.Extensions.canShow
import com.android.ar.ruler.utils.Extensions.setOnOneClickListener
import com.android.ar.ruler.utils.Extensions.showToast
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.system.exitProcess

class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeBinding

    // Note: Use viewModels() if you don't want to share data between fragments
    private val homeViewModel: HomeViewModel by viewModels()

    // Note: Use activityViewModels() if you want to share data between fragments
    // private val homeSharedViewModel: SharedViewModel by sharedViewModel() //using koin
    // OR
    private val homeSharedViewModel: SharedViewModel by activityViewModels() //using androidx

    private val bottomSheetDialog by lazy {
        context?.let { BottomSheetDialog(it) }
    }


    /*** Overrides */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentHomeBinding.inflate(inflater, container, false)

        adjustFontScale(resources.configuration)

        nativeAd()

        listeners()

        observers()

        getDataFromBundles()

        backPressCallback()

        get13NotificationPermission()

        return binding.root
    }

    /*** Functions */

    private fun getDataFromBundles() {
        // how to get data from bundle
        arguments?.let { argument ->
            Log.d("HomeFragment", "${argument.getBoolean(IS_PIN_CREATED)}")
        }
    }

    private fun listeners() {
        binding.btnSharedViewModel.setOnOneClickListener {
            homeSharedViewModel.sendMessage((0..100).random())
        }
    }

    private fun observers() {
        //Note: How to check that binding is initialized or not before using it
        if (::binding.isInitialized){
            homeViewModel.text.observe(viewLifecycleOwner) {
                binding.txtHome.text = it
            }
            homeSharedViewModel.message.observe(viewLifecycleOwner) {
                binding.txtHomeSharedData.text = it
            }
        }
    }

    private fun exitDialogSheet() {
        val sheetBinding = SheetExitBinding.inflate(layoutInflater, null, false)
        bottomSheetDialog?.setContentView(sheetBinding.root)
        bottomSheetDialog?.show()

        if (context?.let { isNetworkAvailable(it) } == true) {
            sheetBinding.nativeContainer.canShow(true)
        }
        if (Constants.SHOW_EXIT_DIALOG_NATIVE_AD == 1) {
            //Note: How to add native ad container height programmatically. it is useful when we manage different ad types with remote config
            val nativeHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, resources.displayMetrics).toInt()
            val admobHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, resources.displayMetrics).toInt()
            sheetBinding.nativeContainer.layoutParams.height = nativeHeight
            sheetBinding.admobContainer.layoutParams.height = admobHeight
            nativeAdExitDialog(sheetBinding)
        } else {
            sheetBinding.nativeContainer.canShow(false)
        }

        sheetBinding.btnExit.setOnClickListener {
            bottomSheetDialog?.dismiss()
            activity?.let {
                it.finishAndRemoveTask()
            }
        }
    }

    private fun backPressCallback(){
        configureBackPress {
            exitDialogSheet()
        }
    }

//    Note: This method called only on Android 13 devices for Post Notification Permission
    private fun get13NotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && context?.let {
                ContextCompat.checkSelfPermission(
                    it, Manifest.permission.POST_NOTIFICATIONS)
            } != PackageManager.PERMISSION_GRANTED) {
            postNotificationPermissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        }
    }

    private val postNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        var once = false
        permissions.entries.forEach {
            val isGranted = it.value
            if (isGranted) {
                if (!once) {
                    context?.let {mContext->
                        Toast.makeText(
                            mContext,
                            getString(R.string.permission_granted),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    once = true
                }
            } else {
                if (activity?.let { it1 ->
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            it1,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    } == true
                ) {
                    // case 4 User has denied permission but not permanently
                } else {
                    // case 5. Permission denied permanently.
//                    permanentlyDeniedPermission(requireActivity())
                }
            }
        }
    }

    /*** AdMob Ads */

    private fun nativeAd() {
        if (context?.let { isNetworkAvailable(it) } == true) {
            binding.nativeContainer.canShow(true)
            activity?.let { fragmentActivity ->
                if (fragmentActivity is MainActivity) {
                    NativeHelper.loadAdsWithConfiguration(
                        fragmentActivity,
                        binding.nativeContainer,
                        binding.admobContainer,
                        200,
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
                                            200
                                        ) { adStatus ->
                                            Log.d("MyNativeAdTag", "nativeAd: $adStatus")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            binding.nativeContainer.canShow(false)
        }
    }

    private fun nativeAdExitDialog(sheetBinding: SheetExitBinding) {
        if (context?.let { isNetworkAvailable(it) } == true) {
            activity?.let { fragmentActivity ->
                if (fragmentActivity is MainActivity) {
                    NativeHelper.loadAdsWithConfiguration(
                        fragmentActivity,
                        sheetBinding.nativeContainer,
                        sheetBinding.admobContainer,
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
                                            sheetBinding.nativeContainer,
                                            sheetBinding.admobContainer,
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
            sheetBinding.nativeContainer.canShow(false)
        }
    }
}