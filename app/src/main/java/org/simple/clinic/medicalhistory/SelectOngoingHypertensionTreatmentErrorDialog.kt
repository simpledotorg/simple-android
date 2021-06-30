package org.simple.clinic.medicalhistory

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.simple.clinic.R

class SelectOngoingHypertensionTreatmentErrorDialog : AppCompatDialogFragment() {

  companion object {

    private const val FRAGMENT_TAG = "select_ongoing_hypertension_treatment_error_dialog"

    fun show(fragmentManager: FragmentManager) {
      val fragment = SelectOngoingHypertensionTreatmentErrorDialog()

      fragment.show(fragmentManager, FRAGMENT_TAG)
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.select_ongoing_hypertension_treatment_title)
        .setMessage(R.string.select_ongoing_hypertension_treatment_message)
        .setPositiveButton(R.string.select_ongoing_hypertension_treatment_positive_action, null)
        .create()
  }
}
