package org.simple.clinic.scanid.scannedqrcode

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.simple.clinic.R

class NationalHealthIDErrorDialog : AppCompatDialogFragment() {
  companion object {

    private const val FRAGMENT_TAG = "national_health_id_error_alert"

    fun show(fragmentManager: FragmentManager) {
      val fragment = NationalHealthIDErrorDialog()

      fragment.show(fragmentManager, FRAGMENT_TAG)
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext())
        .setMessage(getString(R.string.nhid_error_cannot_assign_to_patient_with_existing_id_message))
        .setPositiveButton(getString(R.string.nhid_error_cannot_assign_to_patient_with_existing_ok), null)
        .create()
  }
}
