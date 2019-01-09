package org.simple.clinic.home.patients

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.FragmentManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import android.view.View
import org.simple.clinic.R

class LoggedOutOnOtherDeviceDialog : AppCompatDialogFragment() {

  companion object {
    fun show(fragmentManager: androidx.fragment.app.FragmentManager) {
      (fragmentManager.findFragmentByTag("logged_out_on_other_device_alert") as LoggedOutOnOtherDeviceDialog?)?.dismiss()

      val fragment = LoggedOutOnOtherDeviceDialog()
      // Cancellable on the dialog builder is ignored. We have to use this.
      fragment.isCancelable = false
      fragment.show(fragmentManager, "logged_out_on_other_device_alert")
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(requireContext())
        .setTitle(R.string.patients_loggedoutalert_title)
        .setMessage(R.string.patients_loggedoutalert_message)
        .setPositiveButton(R.string.patients_loggedoutalert_dismiss) { _, _ ->
          dialog?.ownerActivity?.findViewById<View>(android.R.id.content)?.let { view ->
            com.google.android.material.snackbar.Snackbar.make(view, R.string.patients_you_are_now_logged_in, com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
          }
        }
        .create()
  }
}
