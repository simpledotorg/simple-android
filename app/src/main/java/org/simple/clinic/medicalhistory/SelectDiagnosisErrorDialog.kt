package org.simple.clinic.medicalhistory

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import org.simple.clinic.R

class SelectDiagnosisErrorDialog : AppCompatDialogFragment() {

  companion object {

    private const val FRAGMENT_TAG = "select_diagnosis_error_alert"

    fun show(fragmentManager: FragmentManager) {
      val fragment = SelectDiagnosisErrorDialog()

      fragment.show(fragmentManager, FRAGMENT_TAG)
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(getString(R.string.select_diagnosis_error_diagnosis_required))
        .setMessage(getString(R.string.select_diagnosis_error_enter_diagnosis_both_hypertension_diabetes))
        .setPositiveButton(getString(R.string.select_diagnosis_error_ok), null)
        .create()
  }
}
