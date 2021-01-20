package org.simple.clinic.drugs.selection.entry.confirmremovedialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.injector
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class ConfirmRemovePrescriptionDialog : AppCompatDialogFragment(), UiActions {

  @Inject
  lateinit var prescriptionRepository: PrescriptionRepository

  @Inject
  lateinit var effectHandlerFactory: ConfirmRemovePrescriptionDialogEffectHandler.Factory

  private val screenDestroys = PublishSubject.create<ScreenDestroyed>()

  private val prescriptionUuidToDelete by unsafeLazy {
    requireArguments().getSerializable(KEY_PRESCRIPTION_UUID) as UUID
  }

  private val dialogEvents = PublishSubject.create<UiEvent>()
  private val events by unsafeLazy {
    Observable
        .merge(
            screenDestroys,
            removeClicks()
        )
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate: MobiusDelegate<ConfirmRemovePrescriptionDialogModel, ConfirmRemovePrescriptionDialogEvent, ConfirmRemovePrescriptionDialogEffect> by unsafeLazy {
    MobiusDelegate.forActivity(
        events = dialogEvents.ofType(),
        defaultModel = ConfirmRemovePrescriptionDialogModel.create(prescriptionUuidToDelete),
        update = ConfirmRemovePrescriptionDialogUpdate(),
        effectHandler = effectHandlerFactory.create(this).build()
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    delegate.onRestoreInstanceState(savedInstanceState)
  }

  @SuppressLint("CheckResult")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Simple_MaterialAlertDialog_Destructive)
        .setTitle(R.string.customprescription_delete_title)
        .setMessage(R.string.customprescription_delete_message)
        .setPositiveButton(R.string.customprescription_delete_confirm, null)
        .setNegativeButton(R.string.customprescription_delete_cancel, null)
        .create()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    screenDestroys.onNext(ScreenDestroyed())
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  @SuppressLint("CheckResult")
  override fun onResume() {
    super.onResume()
    events
        .takeUntil(screenDestroys)
        .subscribe(dialogEvents::onNext)
  }

  override fun onStop() {
    super.onStop()
    delegate.stop()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun closeDialog() {
    dismiss()
  }

  private fun removeClicks(): Observable<UiEvent> {
    val button = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)
    return button.clicks().map { RemovePrescriptionClicked }
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

  interface Injector {
    fun inject(target: ConfirmRemovePrescriptionDialog)
  }
}
