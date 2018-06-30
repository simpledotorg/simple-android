package org.simple.clinic.drugs.selection

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import io.reactivex.schedulers.Schedulers.io
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.drugs.PrescriptionRepository
import java.util.UUID
import javax.inject.Inject

class ConfirmDeletePrescriptionDialog : DialogFragment() {

  @Inject
  lateinit var prescriptionRepository: PrescriptionRepository

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(requireContext())
        .setTitle(R.string.customprescription_delete_title)
        .setMessage(R.string.customprescription_delete_message)
        .setPositiveButton(R.string.customprescription_delete_confirm) { _, _ -> deletePrescription() }
        .setNegativeButton(R.string.customprescription_delete_cancel) { _, _ -> }
        .create()
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    TheActivity.component.inject(this)
  }

  private fun deletePrescription() {
    val prescriptionUuidToDelete = arguments!!.getSerializable(KEY_PRESCRIPTION_UUID) as UUID

    prescriptionRepository.softDeletePrescription(prescriptionUuidToDelete)
        .subscribeOn(io())
        .subscribe()
  }

  companion object {
    private const val TAG = "BloodPressureEntrySheetFragment"
    private const val KEY_PRESCRIPTION_UUID = "prescriptionUuid"

    fun showForPrescription(prescriptionUuid: UUID, fragmentManager: FragmentManager) {
      val existingFragment = fragmentManager.findFragmentByTag(TAG)

      if (existingFragment != null) {
        fragmentManager
            .beginTransaction()
            .remove(existingFragment)
            .commitNowAllowingStateLoss()
      }

      val fragment = ConfirmDeletePrescriptionDialog()
      val args = Bundle(1)
      args.putSerializable(KEY_PRESCRIPTION_UUID, prescriptionUuid)
      fragment.arguments = args

      fragmentManager
          .beginTransaction()
          .add(fragment, TAG)
          .commitNowAllowingStateLoss()
    }
  }
}
