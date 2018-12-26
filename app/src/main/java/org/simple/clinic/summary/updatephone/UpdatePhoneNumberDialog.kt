package org.simple.clinic.summary.updatephone

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import android.widget.Button
import android.widget.EditText
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.setTextAndCursor
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
  private val cancelButton by bindView<Button>(R.id.updatephone_cancel)
  private val saveButton by bindView<Button>(R.id.updatephone_save)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    TheActivity.component.inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(requireContext())
        .setTitle(R.string.patientsummary_updatephone_dialog_title)
        .setMessage(R.string.patientsummary_updatephone_dialog_message)
        .setView(R.layout.dialog_patientsummary_updatephone)
        .create()
  }

  @SuppressLint("CheckResult")
  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

//    cancelButton.setOnClickListener {
//      dismiss()
//    }
//
//    Observable.merge(dialogCreates(), saveClicks())
//        .observeOn(io())
//        .compose(controller)
  }

  private fun dialogCreates(): Observable<UiEvent> {
    val patientUuid = arguments!!.getSerializable(KEY_PATIENT_UUID) as PatientUuid
    return Observable.just(UpdatePhoneNumberDialogCreated(patientUuid))
  }

  private fun saveClicks() =
      RxView
          .clicks(saveButton)
          .map { UpdatePhoneNumberSaveClicked(number = numberEditText.text.toString()) }

  fun showIncompletePhoneNumberError() {
    TODO()
  }

  fun preFillPhoneNumber(number: String) {
    numberEditText.setTextAndCursor(number)
  }
}
