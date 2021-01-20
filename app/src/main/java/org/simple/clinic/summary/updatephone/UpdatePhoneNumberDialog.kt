package org.simple.clinic.summary.updatephone

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.dialog_patientsummary_updatephone.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class UpdatePhoneNumberDialog : AppCompatDialogFragment(), UpdatePhoneNumberDialogUi, UpdatePhoneNumberUiActions {

  companion object {
    private const val FRAGMENT_TAG = "UpdatePhoneNumberDialog"
    private const val KEY_PATIENT_UUID = "patientUuid"

    fun show(patientUuid: PatientUuid, fragmentManager: FragmentManager) {
      val existingFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)

      if (existingFragment != null) {
        fragmentManager
            .beginTransaction()
            .remove(existingFragment)
            .commitNowAllowingStateLoss()
      }

      val fragment = UpdatePhoneNumberDialog().apply {
        isCancelable = false
        arguments = Bundle(1).apply {
          putSerializable(KEY_PATIENT_UUID, patientUuid)
        }
      }

      fragmentManager
          .beginTransaction()
          .add(fragment, FRAGMENT_TAG)
          .commitNowAllowingStateLoss()
    }
  }

  @Inject
  lateinit var effectHandlerFactory: UpdatePhoneNumberEffectHandler.Factory

  private val screenDestroys = PublishSubject.create<ScreenDestroyed>()
  private val dialogEvents = PublishSubject.create<UiEvent>()
  private val events by unsafeLazy {
    val cancelButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_NEGATIVE)
    val saveButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)

    Observable
        .merge(
            dialogCreates(),
            cancelClicks(cancelButton),
            saveClicks(saveButton)
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val patientUuid = requireArguments().getSerializable(KEY_PATIENT_UUID) as PatientUuid
    val uiRenderer = UpdatePhoneNumberUiRenderer(this)

    MobiusDelegate.forActivity(
        events = dialogEvents.ofType(),
        defaultModel = UpdatePhoneNumberModel.create(patientUuid),
        init = UpdatePhoneNumberInit(),
        update = UpdatePhoneNumberUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    delegate.onRestoreInstanceState(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  @SuppressLint("CheckResult", "InflateParams")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val layout = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_patientsummary_updatephone, null)

    return MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.patientsummary_updatephone_dialog_title)
        .setMessage(R.string.patientsummary_updatephone_dialog_message)
        .setView(layout)
        .setPositiveButton(R.string.patientsummary_updatephone_save, null)
        .setNegativeButton(R.string.patientsummary_updatephone_cancel, null)
        .create()
  }

  override fun onStart() {
    super.onStart()
    dialog!!.numberEditText!!.showKeyboard()
    delegate.start()
  }

  override fun onStop() {
    delegate.stop()
    super.onStop()
  }

  @SuppressLint("CheckResult")
  override fun onResume() {
    super.onResume()
    events
        .takeUntil(screenDestroys)
        .subscribe(dialogEvents::onNext)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    screenDestroys.onNext(ScreenDestroyed())
  }

  private fun dialogCreates(): Observable<UiEvent> {
    return Observable.just(ScreenCreated())
  }

  private fun cancelClicks(cancelButton: Button) =
      cancelButton
          .clicks()
          .map { UpdatePhoneNumberCancelClicked }

  private fun saveClicks(saveButton: Button) =
      saveButton
          .clicks()
          .map { UpdatePhoneNumberSaveClicked(number = dialog!!.numberEditText!!.text?.toString().orEmpty()) }

  override fun showBlankPhoneNumberError() {
    dialog?.phoneInputLayout?.error = getString(R.string.patientsummary_updatephone_error_phonenumber_empty)
  }

  override fun showPhoneNumberTooShortError(minimumAllowedNumberLength: Int) {
    dialog!!.phoneInputLayout!!.error = getString(R.string.patientsummary_updatephone_error_phonenumber_length_less, minimumAllowedNumberLength.toString())
  }

  override fun showPhoneNumberTooLongError(maximumRequiredNumberLength: Int) {
    dialog!!.phoneInputLayout!!.error = getString(R.string.patientsummary_updatephone_error_phonenumber_length_more, maximumRequiredNumberLength.toString())
  }

  override fun preFillPhoneNumber(number: String) {
    dialog!!.numberEditText!!.setTextAndCursor(number)
  }

  override fun closeDialog() {
    dismiss()
  }

  interface Injector {
    fun inject(target: UpdatePhoneNumberDialog)
  }
}
