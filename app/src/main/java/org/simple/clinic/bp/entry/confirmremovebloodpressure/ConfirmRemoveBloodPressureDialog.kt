package org.simple.clinic.bp.entry.confirmremovebloodpressure

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class ConfirmRemoveBloodPressureDialog : AppCompatDialogFragment(), ConfirmRemoveBloodPressureDialogUiActions {
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
  lateinit var effectHandlerFactory: ConfirmRemoveBloodPressureEffectHandler.Factory

  private var removeBloodPressureListener: RemoveBloodPressureListener? = null

  private val dialogEvents = PublishSubject.create<UiEvent>()

  private val events by unsafeLazy {
    dialogEvents
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val bloodPressureMeasurementUuid = requireArguments().getSerializable(KEY_BP_UUID) as UUID

    MobiusDelegate.forActivity(
        events = events.ofType(),
        defaultModel = ConfirmRemoveBloodPressureModel.create(bloodPressureMeasurementUuid),
        update = ConfirmRemoveBloodPressureUpdate(),
        effectHandler = effectHandlerFactory.create(this).build()
    )
  }

  @SuppressLint("CheckResult")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(requireContext(), R.style.Clinic_V2_DialogStyle_Destructive)
        .setTitle(R.string.bloodpressureentry_remove_bp_title)
        .setMessage(R.string.bloodpressureentry_remove_bp_message)
        .setPositiveButton(R.string.bloodpressureentry_remove_bp_confirm) { _, _ ->
          removeBloodPressureListener?.onBloodPressureRemoved()
          dialogEvents.onNext(ConfirmRemoveBloodPressureDialogRemoveClicked)
        }
        .setNegativeButton(R.string.bloodpressureentry_remove_bp_cancel, null)
        .create()
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    delegate.stop()
    super.onStop()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
    removeBloodPressureListener = context as? RemoveBloodPressureListener
    if (removeBloodPressureListener == null) {
      throw ClassCastException("$context must implement RemoveBloodPressureListener")
    }
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
