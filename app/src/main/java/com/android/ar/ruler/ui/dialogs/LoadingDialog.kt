package com.android.ar.ruler.ui.dialogs

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.android.ar.ruler.R
import java.lang.IllegalArgumentException

object LoadingDialog {

    private var dialog: Dialog? = null

    fun showLoadingDialog(context: Activity?, text: Boolean = false) {
        if (dialog == null) {
            dialog = context?.let { Dialog(it) }

            if (!text) {
                dialog?.setContentView(R.layout.dialog_loading)
            } else {
                dialog?.setContentView(R.layout.loading_progress)
            }

            dialog?.setCancelable(false)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        if (dialog?.isShowing == false)
            dialog?.show()

    }

    fun hideLoadingDialog() {
        try {
            dialog?.dismiss()
            dialog = null
        } catch (e: IllegalArgumentException) {
        }
    }
}