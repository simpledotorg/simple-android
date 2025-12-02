package org.simple.clinic.medicalhistory

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.simple.clinic.R

class SelectDiagnosisOrReferralRequiredErrorDialog : AppCompatDialogFragment() {

  companion object {

    private const val FRAGMENT_TAG = "select_diagnosis_or_referral_required__error_alert"

    fun show(fragmentManager: FragmentManager) {
      val fragment = SelectDiagnosisOrReferralRequiredErrorDialog()

      fragment.show(fragmentManager, FRAGMENT_TAG)
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(getString(R.string.select_diagnosis_error_diagnosis_required))
        .setMessage(getString(R.string.select_diagnosis_or_referral_required_error_message))
        .setPositiveButton(getString(R.string.select_diagnosis_error_ok), null)
        .create()
  }
}
