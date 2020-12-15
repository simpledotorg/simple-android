package org.simple.clinic.summary.addphone

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.DialogPatientsummaryAddphoneBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class AddPhoneNumberDialog : AppCompatDialogFragment(), AddPhoneNumberUi, UiActions {

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
  lateinit var effectHandlerFactory: AddPhoneNumberEffectHandler.Factory

  private val patientUuid by unsafeLazy {
    requireArguments().getSerializable(KEY_PATIENT_UUID) as PatientUuid
  }

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
        defaultModel = AddPhoneNumberModel.create(patientUuid),
        update = AddPhoneNumberUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  private var layout: View? = null
  private var binding: DialogPatientsummaryAddphoneBinding? = null

  private val phoneNumberEditText
    get() = binding!!.phoneNumberEditText

  private val phoneNumberInputLayout
    get() = binding!!.phoneNumberInputLayout

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requireContext().injector<Injector>().inject(this)
    delegate.onRestoreInstanceState(savedInstanceState)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return layout
  }

  @SuppressLint("CheckResult", "InflateParams")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val layoutInflater = LayoutInflater.from(requireContext())
    binding = DialogPatientsummaryAddphoneBinding.inflate(layoutInflater)
    layout = binding?.root

    return AlertDialog.Builder(requireContext())
        .setTitle(R.string.patientsummary_addphone_dialog_title)
        .setMessage(R.string.patientsummary_addphone_dialog_message)
        .setView(layout)
        .setPositiveButton(R.string.patientsummary_addphone_save, null)
        .setNegativeButton(R.string.patientsummary_addphone_cancel, null)
        .create()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    delegate.onSaveInstanceState(outState)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    phoneNumberEditText.showKeyboard()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    screenDestroys.onNext(ScreenDestroyed())
    binding = null
    layout = null
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

  private fun saveClicks(): Observable<UiEvent> {
    val saveButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)

    return saveButton
        .clicks()
        .map { AddPhoneNumberSaveClicked(number = phoneNumberEditText.text.toString()) }
  }

  override fun showPhoneNumberBlank() {
    phoneNumberInputLayout.error = getString(R.string.patientsummary_addphone_error_phonenumber_empty)
  }

  override fun showPhoneNumberTooShortError(requiredNumberLength: Int) {
    phoneNumberInputLayout.error = getString(R.string.patientsummary_addphone_error_phonenumber_length_less, requiredNumberLength.toString())
  }

  override fun showPhoneNumberTooLongError(requiredNumberLength: Int) {
    phoneNumberInputLayout.error = getString(R.string.patientsummary_addphone_error_phonenumber_length_more, requiredNumberLength.toString())
  }

  override fun clearPhoneNumberError() {
    phoneNumberInputLayout.error = null
  }

  override fun closeDialog() {
    dismiss()
  }

  interface Injector {
    fun inject(target: AddPhoneNumberDialog)
  }
}
