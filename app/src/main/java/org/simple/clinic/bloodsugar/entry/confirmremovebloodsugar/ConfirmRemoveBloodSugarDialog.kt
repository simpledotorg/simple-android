package org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar

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
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.util.unsafeLazy
import java.util.UUID
import javax.inject.Inject

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

  @Inject
  lateinit var effectHandler: ConfirmRemoveBloodSugarEffectHandler.Factory

  @Inject
  lateinit var crashReporter: CrashReporter

  private val events = PublishSubject.create<ConfirmRemoveBloodSugarEvent>()

  private val delegate by unsafeLazy {
    val bloodSugarMeasurementUuid = requireArguments().getSerializable(KEY_BLOOD_SUGAR_UUID) as UUID

    MobiusDelegate.forActivity(
        events = events.ofType(),
        defaultModel = ConfirmRemoveBloodSugarModel.create(bloodSugarMeasurementUuid),
        update = ConfirmRemoveBloodSugarUpdate(),
        effectHandler = effectHandler.create(this).build()
    )
  }

  private var removeBloodSugarListener: RemoveBloodSugarListener? = null

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<ConfirmRemoveBloodSugarDialogInjector>().inject(this)
    removeBloodSugarListener = context as? RemoveBloodSugarListener
    if (removeBloodSugarListener == null) {
      throw ClassCastException("$context must implement RemoveBloodSugarListener")
    }
  }

  override fun onDetach() {
    removeBloodSugarListener = null
    super.onDetach()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    delegate.onRestoreInstanceState(savedInstanceState)
  }

  @SuppressLint("CheckResult")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(requireContext(), R.style.Clinic_V2_DialogStyle_Destructive)
        .setTitle(R.string.bloodsugarentry_remove_blood_sugar_title)
        .setMessage(R.string.bloodsugarentry_remove_blood_sugar_message)
        .setPositiveButton(R.string.bloodsugarentry_remove_blood_sugar_confirm) { _, _ ->
          removeBloodSugarListener?.onBloodSugarRemoved()
          events.onNext(RemoveBloodSugarClicked)
        }
        .setNegativeButton(R.string.bloodsugarentry_remove_blood_sugar_cancel, null)
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

  override fun onSaveInstanceState(outState: Bundle) {
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun closeDialog() {
    dismiss()
  }

  interface RemoveBloodSugarListener {
    fun onBloodSugarRemoved()
  }
}
