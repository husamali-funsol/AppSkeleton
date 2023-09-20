package com.android.ar.ruler.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

object Extensions {

    fun View.setOnOneClickListener(debounceTime: Long = 600L, action: () -> Unit) {
        this.setOnClickListener(object : View.OnClickListener {
            private var lastClickTime: Long = 0

            override fun onClick(v: View) {
                if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
                else action()

                lastClickTime = SystemClock.elapsedRealtime()
            }
        })
    }

    inline fun Context.showToast(message:()->String){
        Toast.makeText(this, message() , Toast.LENGTH_LONG).show()
    }

    inline fun Fragment.showToast(message:()->String){
        Toast.makeText(this.context, message() , Toast.LENGTH_LONG).show()
    }

    fun Context.checkConnection() : Flow<Boolean> = callbackFlow{
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(true).isSuccess
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(false).isSuccess
            }
        }
        val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        manager.registerNetworkCallback(
            NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build(),callback)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }

    fun EditText.textChange() : Flow<Editable?> = callbackFlow{
        val callback = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                trySend(s).isSuccess
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

        }
        addTextChangedListener(callback)
        awaitClose { removeTextChangedListener(callback) }
    }

    /**
     * Common extensions.
     */

    inline fun <T: Any,R> T?.withNotNull(block:(T) -> R): R?{
        return this?.let (block)
    }

    fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable){
            return bitmap
        }

        val bitmap = Bitmap.createBitmap(intrinsicWidth,intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0,0,canvas.width,canvas.height)
        draw(canvas)

        return bitmap
    }

    private fun containsRussiaTimeZone(timeZoneIdList: Array<String>, timeZone: String): Boolean {
        return timeZoneIdList.any { it == timeZone }
    }

    fun Any?.debug() {
        Log.d("tag", "$this")
    }

    /**
     * View's visibility extensions.
     */

    fun View.visible() {
        visibility = View.VISIBLE
    }

    fun View.gone() {
        visibility = View.GONE
    }

    fun View.invisible() {
        visibility = View.INVISIBLE
    }

    fun View.canShow(isVisible: Boolean) {
        visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    /**
     * SnackBar's extensions.
     */

    fun View.showSmallSnackBar(text: String) {
        Handler(Looper.getMainLooper()).post{
            Snackbar.make(this, text, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun View.showLongSnackBar(text: String) {
        Handler(Looper.getMainLooper()).post{
            Snackbar.make(this, text, Snackbar.LENGTH_LONG).show()
        }
    }

    fun View.showIndefiniteSnackBar(text: String) {
        Handler(Looper.getMainLooper()).post {
            Snackbar.make(this, text, Snackbar.LENGTH_INDEFINITE).show()
        }
    }

    /**
     * Toast extensions.
     */

    fun Context.showSmallLengthToast(text: String) {
        Handler(Looper.getMainLooper()).post{
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }
    }

    fun Context.showLongLengthToast(text: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Collections & permissions extensions.
     */

    fun <T:Any> ArrayList<T>.refreshList(items: List<T>): ArrayList<T> {
        this.clear()
        this.addAll(items)
        return this
    }

    fun <T:Any> ArrayList<T>.addOnlyNewItems(items: List<T>): ArrayList<T> {
        items.forEach { if(!this.contains(it)) this.add(it) }
        return this
    }

    fun Activity.hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * viewModel extensions.
     */

//    fun ViewModel.loadRequest(context: Context, block: suspend () -> Unit): Job {
//        return viewModelScope.launch {
//            try {
//                block()
//            }catch (ex: Exception) {
//                Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    /**
     * Fragments extensions.
     */

//    fun <T> Fragment.setBackStackData(key: String, data: T, doBack: Boolean = false){
//        findNavController().previousBackStackEntry?.savedStateHandle?.set(key,data)
//        if (doBack){
//            findNavController().popBackStack()
//        }
//    }

//    fun <T> Fragment.getBackStackData(key: String, singleCall: Boolean = true, result: (T) -> (Unit)){
//        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<T>(key)?.observe(viewLifecycleOwner){
//            result(it)
//            //if not removed then when click back without set data. it will return previous data
//            if (singleCall){
//                findNavController().currentBackStackEntry?.savedStateHandle?.remove<T>(key)
//            }
//        }
//    }

//    suspend fun <T:Any> ControlledRunner<T>.cancelLastRequestThenRun(block: suspend () -> T): T {
//        return cancelPreviousThenRun(block)
//    }
//
//    suspend fun <T:Any> ControlledRunner<T>.joinLastRequestThenRun(block: suspend () -> T): T {
//        return joinPreviousOrRun(block)
//    }
}