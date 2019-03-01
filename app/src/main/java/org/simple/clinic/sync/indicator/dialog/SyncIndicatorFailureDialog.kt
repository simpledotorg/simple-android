package org.simple.clinic.sync.indicator.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import org.simple.clinic.R

class SyncIndicatorFailureDialog : AppCompatDialogFragment() {

  companion object {
    private const val KEY_MESSAGE = "failure_message"

    fun show(fragmentManager: FragmentManager, message: String) {
      (fragmentManager.findFragmentByTag("sync_indicator_failure_dialog") as SyncIndicatorFailureDialog?)?.dismiss()

      val fragment = SyncIndicatorFailureDialog().apply {
        arguments = Bundle(1).apply {
          putSerializable(KEY_MESSAGE, message)
        }
      }
      // Cancellable on the dialog builder is ignored. We have to use this.
      fragment.isCancelable = false
      fragment.show(fragmentManager, "sync_indicator_failure_dialog")
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val message = arguments!!.getString(KEY_MESSAGE)

    return AlertDialog.Builder(requireContext(), R.style.Clinic_V2_DialogStyle)
        .setMessage(message)
        .setPositiveButton(getString(R.string.sync_indocator_dialog_button_text), null)
        .create()
  }
}
