package org.simple.clinic.summary.addphone

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bindUiToController
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class AddPhoneNumberDialog : AppCompatDialogFragment(), AddPhoneNumberUi {

  companion object {
    private const val FRAGMENT_TAG = "AddPhoneNumberDialog"
    private const val KEY_PATIENT_UUID = "patientUuid"

    fun show(patientUuid: PatientUuid, fragmentManager: FragmentManager) {
      val existingFragment = fragmentManager.findFragmentByTag(FRAGMENT_TAG)

      if (existingFragment != null) {
        fragmentManager
            .beginTransaction()
            .remove(existingFragment)
            .commitNowAllowingStateLoss()
      }

      val fragment = AddPhoneNumberDialog().apply {
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
  lateinit var controller: AddPhoneNumberDialogController.Factory

  @Inject
  lateinit var effectHandlerFactory: AddPhoneNumberEffectHandler.Factory

  private val phoneInputLayout by bindView<TextInputLayout>(R.id.addphone_phone_inputlayout)
  private val numberEditText by bindView<EditText>(R.id.addphone_phone)

  private val onStarts = PublishSubject.create<Any>()
  private val dialogEvents = PublishSubject.create<UiEvent>()

  private val screenDestroys = PublishSubject.create<ScreenDestroyed>()
  private val events by unsafeLazy {
    saveClicks()
        .takeUntil(screenDestroys)
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate: MobiusDelegate<AddPhoneNumberModel, AddPhoneNumberEvent, AddPhoneNumberEffect> by unsafeLazy {
    val uiRenderer = AddPhoneNumberUiRender(this)

    MobiusDelegate.forActivity(
        events = dialogEvents.ofType(),
        defaultModel = AddPhoneNumberModel.create(),
        update = AddPhoneNumberUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    TheActivity.component.inject(this)
    delegate.onRestoreInstanceState(savedInstanceState)
  }

  @SuppressLint("CheckResult", "InflateParams")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val layout = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_patientsummary_addphone, null)
    val dialog = AlertDialog.Builder(requireContext())
        .setTitle(R.string.patientsummary_addphone_dialog_title)
        .setMessage(R.string.patientsummary_addphone_dialog_message)
        .setView(layout)
        .setPositiveButton(R.string.patientsummary_addphone_save, null)
        .setNegativeButton(R.string.patientsummary_addphone_cancel, null)
        .create()

    onStarts
        .take(1)
        .subscribe { setupDialog() }

    return dialog
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    delegate.onSaveInstanceState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    screenDestroys.onNext(ScreenDestroyed())
  }

  override fun onStart() {
    super.onStart()
    onStarts.onNext(Any())
    numberEditText.showKeyboard()
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

  private fun setupDialog() {
    val patientUuid = arguments!!.getSerializable(KEY_PATIENT_UUID) as PatientUuid

    bindUiToController(
        ui = this,
        events = dialogEvents.ofType(),
        controller = controller.create(patientUuid),
        screenDestroys = screenDestroys
    )
  }

  private fun saveClicks(): Observable<UiEvent> {
    val saveButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)

    return saveButton
        .clicks()
        .map { AddPhoneNumberSaveClicked(number = numberEditText.text.toString()) }
  }

  override fun showPhoneNumberBlank() {
    phoneInputLayout.error = getString(R.string.patientsummary_addphone_error_phonenumber_empty)
  }

  override fun showPhoneNumberTooShortError(requiredNumberLength: Int) {
    phoneInputLayout.error = getString(R.string.patientsummary_addphone_error_phonenumber_length_less, requiredNumberLength.toString())
  }

  override fun showPhoneNumberTooLongError(requiredNumberLength: Int) {
    phoneInputLayout.error = getString(R.string.patientsummary_addphone_error_phonenumber_length_more, requiredNumberLength.toString())
  }

  override fun closeDialog() {
    dismiss()
  }
}
