package org.simple.clinic.bp.entry.confirmremovebloodpressure

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class ConfirmRemoveBloodPressureDialog : AppCompatDialogFragment(), ConfirmRemoveBloodPressureDialogUi {
  companion object {
    private const val KEY_BP_UUID = "bloodPressureMeasurementUuid"

    fun show(bloodPressureMeasurementUuid: UUID, fragmentManager: FragmentManager) {
      val fragmentTag = "fragment_confirm_remove_blood_pressure"

      val existingFragment = fragmentManager.findFragmentByTag(fragmentTag)

      if (existingFragment != null) {
        fragmentManager
            .beginTransaction()
            .remove(existingFragment)
            .commitNowAllowingStateLoss()
      }

      val args = Bundle()
      args.putSerializable(KEY_BP_UUID, bloodPressureMeasurementUuid)

      val fragment = ConfirmRemoveBloodPressureDialog()
      fragment.arguments = args

      fragmentManager
          .beginTransaction()
          .add(fragment, fragmentTag)
          .commitNowAllowingStateLoss()
    }
  }

  @Inject
  lateinit var controller: ConfirmRemoveBloodPressureDialogController.Factory

  private var removeBloodPressureListener: RemoveBloodPressureListener? = null

  private val screenDestroys = PublishSubject.create<ScreenDestroyed>()
  private val onStarts = PublishSubject.create<Any>()

  @SuppressLint("CheckResult")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = AlertDialog.Builder(requireContext(), R.style.Clinic_V2_DialogStyle_Destructive)
        .setTitle(R.string.bloodpressureentry_remove_bp_title)
        .setMessage(R.string.bloodpressureentry_remove_bp_message)
        .setPositiveButton(R.string.bloodpressureentry_remove_bp_confirm, null)
        .setNegativeButton(R.string.bloodpressureentry_remove_bp_cancel, null)
        .create()

    onStarts
        .take(1)
        .subscribe { setupDialog() }

    return dialog
  }

  override fun onStart() {
    super.onStart()
    onStarts.onNext(Any())
  }

  override fun onDestroyView() {
    super.onDestroyView()
    screenDestroys.onNext(ScreenDestroyed())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
    removeBloodPressureListener = context as? RemoveBloodPressureListener
    if (removeBloodPressureListener == null) {
      throw ClassCastException("$context must implement RemoveBloodPressureListener")
    }
  }

  private fun setupDialog() {
    val bloodPressureMeasurementUuid = arguments!!.getSerializable(KEY_BP_UUID) as UUID

    bindUiToController(
        ui = this,
        events = removeClicks(),
        controller = controller.create(bloodPressureMeasurementUuid),
        screenDestroys = screenDestroys
    )
  }

  private fun removeClicks(): Observable<UiEvent> {
    val button = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)

    return RxView.clicks(button)
        .doOnNext { removeBloodPressureListener?.onBloodPressureRemoved() }
        .map { ConfirmRemoveBloodPressureDialogRemoveClicked }
  }

  override fun closeDialog() {
    dismiss()
  }

  interface RemoveBloodPressureListener {
    fun onBloodPressureRemoved()
  }

  interface Injector {
    fun inject(target: ConfirmRemoveBloodPressureDialog)
  }
}
