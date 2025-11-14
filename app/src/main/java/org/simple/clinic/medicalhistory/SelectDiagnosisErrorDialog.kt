package org.simple.clinic.medicalhistory

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.simple.clinic.R

class SelectDiagnosisErrorDialog : AppCompatDialogFragment() {

  companion object {

    private const val FRAGMENT_TAG = "select_diagnosis_error_alert"
    private const val KEY_DIABETES_MANAGEMENT_ENABLED = "key_diabetes_management_enabled"

    fun show(fragmentManager: FragmentManager, diabetesManagementEnabled: Boolean) {
      val fragment = SelectDiagnosisErrorDialog().apply {
        arguments = Bundle().apply {
          putBoolean(KEY_DIABETES_MANAGEMENT_ENABLED, diabetesManagementEnabled)
        }
      }

      fragment.show(fragmentManager, FRAGMENT_TAG)
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val diabetesManagementEnabled =
        arguments?.getBoolean(KEY_DIABETES_MANAGEMENT_ENABLED) ?: false

    val messageRes = if (diabetesManagementEnabled) {
      R.string.select_diagnosis_error_enter_diagnosis_both_hypertension_diabetes
    } else {
      R.string.select_diagnosis_error_enter_diagnosis_hypertension
    }

    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(getString(R.string.select_diagnosis_error_diagnosis_required))
        .setMessage(getString(messageRes))
        .setPositiveButton(getString(R.string.select_diagnosis_error_ok), null)
        .create()
  }
}
