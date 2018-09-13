package org.simple.clinic.login.applock

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.view.View
import org.simple.clinic.R

class ConfirmResetPinDialog : AppCompatDialogFragment() {

  companion object {
    fun show(fragmentManager: FragmentManager) {
      (fragmentManager.findFragmentByTag("confirm_reset_pin_alert") as ConfirmResetPinDialog?)?.dismiss()

      val fragment = ConfirmResetPinDialog()
      // Cancellable on the dialog builder is ignored. We have to use this.
      fragment.isCancelable = false
      fragment.show(fragmentManager, "confirm_reset_pin_alert")
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(context!!)
        .setTitle(R.string.applock_reset_pin_alert_title)
        .setMessage(R.string.applock_reset_pin_alert_message)
        .setPositiveButton(R.string.applock_reset_pin_alert_confirm) { _, _ ->
          val view = dialog.ownerActivity.findViewById<View>(android.R.id.content)
          val snackbar = Snackbar.make(view, getString(R.string.applock_reset_pin_waiting), Snackbar.LENGTH_INDEFINITE)
          snackbar.show()
        }
        .setNegativeButton(R.string.applock_reset_pin_alert_cancel, null)
        .create()
  }
}
