package org.simple.clinic.home.overdue.removepatient

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.RadioButton
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentCancelReason.InvalidPhoneNumber
import org.simple.clinic.overdue.AppointmentCancelReason.Moved
import org.simple.clinic.overdue.AppointmentCancelReason.MovedToPrivatePractitioner
import org.simple.clinic.overdue.AppointmentCancelReason.Other
import org.simple.clinic.overdue.AppointmentCancelReason.PatientNotResponding
import org.simple.clinic.overdue.AppointmentCancelReason.TransferredToAnotherPublicHospital
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.PrimarySolidButton
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class RemoveAppointmentSheet : BottomSheetActivity() {

  companion object {
    private const val KEY_APPOINTMENT_UUID = "KEY_APPOINTMENT_UUID"
    private const val KEY_PATIENT_UUID = "KEY_PATIENT_UUID"

    fun intent(context: Context, appointmentUuid: UUID, patientUuid: UUID): Intent =
        Intent(context, RemoveAppointmentSheet::class.java)
            .putExtra(RemoveAppointmentSheet.KEY_APPOINTMENT_UUID, appointmentUuid)
            .putExtra(KEY_PATIENT_UUID, patientUuid)
  }

  @Inject
  lateinit var controller: RemoveAppointmentSheetController

  private val alreadyVisitedRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_patient_already_visited)
  private val alreadyVisitedSeparator by bindView<View>(R.id.removeappointment_reason_patient_already_visited_separator)
  private val movedOutRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_patient_moved)
  private val movedOutRadioButtonSeparator by bindView<View>(R.id.removeappointment_reason_patient_moved_separator)
  private val notRespondingRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_patient_not_responding)
  private val invalidPhoneNumberRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_invalid_phone_number)
  private val invalidPhoneNumberSeparator by bindView<View>(R.id.removeappointment_reason_invalid_phone_number_separator)
  private val publicHospitalTransferRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_public_hospital_transfer)
  private val publicHospitalTransferSeparator by bindView<View>(R.id.removeappointment_reason_public_hospital_transfer_separator)
  private val movedToPrivateRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_moved_to_private)
  private val movedToPrivateSeparator by bindView<View>(R.id.removeappointment_reason_moved_to_private_separator)
  private val diedRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_patient_died)
  private val otherReasonRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_other)
  private val reasonSelectedDoneButton by bindView<PrimarySolidButton>(R.id.removeappointment_done_button)

  private val onDestroys = PublishSubject.create<Any>()

  @SuppressLint("CheckResult")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_remove_appointment)
    TheActivity.component.inject(this)

    Observable.mergeArray(sheetCreates(), alreadyVisitedClicks(), cancelReasonClicks(), doneClicks(), patientDiedClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(onDestroys)
        .subscribe { uiChange -> uiChange(this) }
  }

  override fun onDestroy() {
    onDestroys.onNext(Any())
    super.onDestroy()
  }

  private fun sheetCreates(): Observable<UiEvent> {
    val appointmentUuid = intent.extras!!.getSerializable(KEY_APPOINTMENT_UUID) as UUID
    return Observable.just(RemoveAppointmentSheetCreated(appointmentUuid))
  }

  private fun doneClicks() =
      RxView
          .clicks(reasonSelectedDoneButton)
          .map { RemoveReasonDoneClicked }

  private fun alreadyVisitedClicks() =
      RxView
          .clicks(alreadyVisitedRadioButton)
          .map { AlreadyVisitedReasonClicked }

  private fun patientDiedClicks() =
      RxView
          .clicks(diedRadioButton)
          .map { PatientDeadClicked(patientUuid = intent.extras!!.getSerializable(KEY_PATIENT_UUID) as UUID) }

  private fun cancelReasonClicks(): Observable<UiEvent> {
    val buttonToCancelReasons = mapOf(
        movedOutRadioButton to Moved,
        notRespondingRadioButton to PatientNotResponding,
        invalidPhoneNumberRadioButton to InvalidPhoneNumber,
        publicHospitalTransferRadioButton to TransferredToAnotherPublicHospital,
        movedToPrivateRadioButton to MovedToPrivatePractitioner,
        otherReasonRadioButton to Other)

    val reasonClicks = { entry: Map.Entry<View, AppointmentCancelReason> ->
      RxView.clicks(entry.key).map {
        CancelReasonClicked(entry.value)
      }
    }

    return Observable.merge(buttonToCancelReasons.map(reasonClicks))
  }

  fun closeSheet() = finish()

  fun enableDoneButton() {
    reasonSelectedDoneButton.isEnabled = true
  }

  fun setV2ApiReasonsEnabled(v2Enabled: Boolean) {
    invalidPhoneNumberRadioButton.visibility = if (v2Enabled) VISIBLE else GONE
    invalidPhoneNumberSeparator.visibility = if (v2Enabled) VISIBLE else GONE
    publicHospitalTransferRadioButton.visibility = if (v2Enabled) VISIBLE else GONE
    publicHospitalTransferSeparator.visibility = if (v2Enabled) VISIBLE else GONE
    movedToPrivateRadioButton.visibility = if (v2Enabled) VISIBLE else GONE
    movedToPrivateSeparator.visibility = if (v2Enabled) VISIBLE else GONE

    alreadyVisitedRadioButton.visibility = if (v2Enabled) GONE else VISIBLE
    alreadyVisitedSeparator.visibility = if (v2Enabled) GONE else VISIBLE
    movedOutRadioButton.visibility = if (v2Enabled) GONE else VISIBLE
    movedOutRadioButtonSeparator.visibility = if (v2Enabled) GONE else VISIBLE
  }
}
