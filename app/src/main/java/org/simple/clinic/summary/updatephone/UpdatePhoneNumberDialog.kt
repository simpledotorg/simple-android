package org.simple.clinic.summary.updatephone

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.main.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.setTextAndCursor
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class UpdatePhoneNumberDialog : AppCompatDialogFragment() {

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
  lateinit var controller: UpdatePhoneNumberDialogController

  private val phoneInputLayout by bindView<TextInputLayout>(R.id.updatephone_phone_inputlayout)
  private val numberEditText by bindView<EditText>(R.id.updatephone_phone)

  private val onStarts = PublishSubject.create<Any>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    TheActivity.component.inject(this)
  }

  @SuppressLint("CheckResult", "InflateParams")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val layout = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_patientsummary_updatephone, null)

    val dialog = AlertDialog.Builder(requireContext())
        .setTitle(R.string.patientsummary_updatephone_dialog_title)
        .setMessage(R.string.patientsummary_updatephone_dialog_message)
        .setView(layout)
        .setPositiveButton(R.string.patientsummary_updatephone_save, null)
        .setNegativeButton(R.string.patientsummary_updatephone_cancel, null)
        .create()

    onStarts
        .take(1)
        .subscribe { setupDialog(RxView.detaches(layout).map { ScreenDestroyed() }) }

    return dialog
  }

  override fun onStart() {
    super.onStart()
    onStarts.onNext(Any())
    numberEditText.showKeyboard()
  }

  private fun setupDialog(screenDestroys: Observable<ScreenDestroyed>) {
    val cancelButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_NEGATIVE)
    val saveButton = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)

    bindUiToController(
        ui = this,
        events = Observable.merge(
            dialogCreates(),
            cancelClicks(cancelButton),
            saveClicks(saveButton)
        ),
        controller = controller,
        screenDestroys = screenDestroys
    )
  }

  private fun dialogCreates(): Observable<UiEvent> {
    val patientUuid = arguments!!.getSerializable(KEY_PATIENT_UUID) as PatientUuid
    return Observable.just(UpdatePhoneNumberDialogCreated(patientUuid))
  }

  private fun cancelClicks(cancelButton: Button) =
      RxView
          .clicks(cancelButton)
          .map { UpdatePhoneNumberCancelClicked }

  private fun saveClicks(saveButton: Button) =
      RxView
          .clicks(saveButton)
          .map { UpdatePhoneNumberSaveClicked(number = numberEditText.text.toString()) }

  fun showPhoneNumberTooShortError() {
    phoneInputLayout.error = getString(R.string.patientsummary_updatephone_error_phonenumber_length_less)
  }

  fun showPhoneNumberTooLongError() {
    phoneInputLayout.error = getString(R.string.patientsummary_updatephone_error_phonenumber_length_more)
  }

  fun preFillPhoneNumber(number: String) {
    numberEditText.setTextAndCursor(number)
  }
}
