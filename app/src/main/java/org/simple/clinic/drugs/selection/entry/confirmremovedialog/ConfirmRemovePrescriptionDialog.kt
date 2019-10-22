package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.R
import org.simple.clinic.main.TheActivity
import org.simple.clinic.bp.entry.confirmremovebloodpressure.ConfirmRemovePrescriptionDialogCreated
import org.simple.clinic.bp.entry.confirmremovebloodpressure.RemovePrescriptionClicked
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class ConfirmRemovePrescriptionDialog : AppCompatDialogFragment() {

  @Inject
  lateinit var prescriptionRepository: PrescriptionRepository

  @Inject
  lateinit var controller: ConfirmRemovePrescriptionDialogController

  private val onStarts = PublishSubject.create<Any>()
  private val screenDestroys = PublishSubject.create<ScreenDestroyed>()

  @SuppressLint("CheckResult")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = AlertDialog.Builder(requireContext(), R.style.Clinic_V2_DialogStyle_Destructive)
        .setTitle(R.string.customprescription_delete_title)
        .setMessage(R.string.customprescription_delete_message)
        .setPositiveButton(R.string.customprescription_delete_confirm, null)
        .setNegativeButton(R.string.customprescription_delete_cancel, null)
        .create()

    onStarts
        .take(1)
        .flatMap { setupDialog() }
        .takeUntil(screenDestroys)
        .subscribe { uiChange -> uiChange(this) }

    return dialog
  }

  private fun setupDialog(): Observable<UiChange> {
    return Observable.merge(dialogCreates(), screenDestroys, removeClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    TheActivity.component.inject(this)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    screenDestroys.onNext(ScreenDestroyed())
  }

  override fun onStart() {
    super.onStart()
    onStarts.onNext(Any())
  }

  private fun dialogCreates(): Observable<UiEvent> {
    val prescriptionUuidToDelete = arguments!!.getSerializable(KEY_PRESCRIPTION_UUID) as UUID
    return Observable.just(ConfirmRemovePrescriptionDialogCreated(prescriptionUuidToDelete))
  }

  private fun removeClicks(): Observable<UiEvent> {
    val button = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
    return RxView.clicks(button).map { RemovePrescriptionClicked }
  }

  companion object {
    private const val TAG = "ConfirmRemovePrescriptionDialog"
    private const val KEY_PRESCRIPTION_UUID = "prescriptionUuid"

    fun showForPrescription(prescriptionUuid: UUID, fragmentManager: FragmentManager) {
      val existingFragment = fragmentManager.findFragmentByTag(TAG)

      if (existingFragment != null) {
        fragmentManager
            .beginTransaction()
            .remove(existingFragment)
            .commitNowAllowingStateLoss()
      }

      val fragment = ConfirmRemovePrescriptionDialog()
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
