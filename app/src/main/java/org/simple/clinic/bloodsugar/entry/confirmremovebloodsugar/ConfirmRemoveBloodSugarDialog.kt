package org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import org.simple.clinic.R
import org.simple.clinic.di.injector
import java.util.UUID

class ConfirmRemoveBloodSugarDialog : AppCompatDialogFragment(), ConfirmRemoveBloodSugarUiActions {
  companion object {
    private const val KEY_BLOOD_SUGAR_UUID = "bloodSugarMeasurementUuid"

    fun show(bloodSugarMeasurementUuid: UUID, fragmentManager: FragmentManager) {
      val fragmentTag = "fragment_confirm_remove_blood_sugar"

      val existingFragment = fragmentManager.findFragmentByTag(fragmentTag)

      if (existingFragment != null) {
        fragmentManager
            .beginTransaction()
            .remove(existingFragment)
            .commitNowAllowingStateLoss()
      }

      val args = Bundle()
      args.putSerializable(KEY_BLOOD_SUGAR_UUID, bloodSugarMeasurementUuid)

      val fragment = ConfirmRemoveBloodSugarDialog()
      fragment.arguments = args

      fragmentManager
          .beginTransaction()
          .add(fragment, fragmentTag)
          .commitNowAllowingStateLoss()
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(requireContext(), R.style.Clinic_V2_DialogStyle_Destructive)
        .setTitle(R.string.bloodsugarentry_remove_blood_sugar_title)
        .setMessage(R.string.bloodsugarentry_remove_blood_sugar_message)
        .setPositiveButton(R.string.bloodsugarentry_remove_blood_sugar_confirm, null)
        .setNegativeButton(R.string.bloodsugarentry_remove_blood_sugar_cancel, null)
        .create()
  }

  override fun closeDialog() {
    dismiss()
  }
}
