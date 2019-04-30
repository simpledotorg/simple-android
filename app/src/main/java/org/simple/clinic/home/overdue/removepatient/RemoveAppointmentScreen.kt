package org.simple.clinic.home.overdue.removepatient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentCancelReason.InvalidPhoneNumber
import org.simple.clinic.overdue.AppointmentCancelReason.MovedToPrivatePractitioner
import org.simple.clinic.overdue.AppointmentCancelReason.Other
import org.simple.clinic.overdue.AppointmentCancelReason.PatientNotResponding
import org.simple.clinic.overdue.AppointmentCancelReason.TransferredToAnotherPublicHospital
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class RemoveAppointmentScreen : AppCompatActivity() {

  companion object {
    private const val KEY_APPOINTMENT_UUID = "KEY_APPOINTMENT_UUID"
    private const val KEY_PATIENT_UUID = "KEY_PATIENT_UUID"

    fun intent(context: Context, appointmentUuid: UUID, patientUuid: UUID): Intent =
        Intent(context, RemoveAppointmentScreen::class.java)
            .putExtra(RemoveAppointmentScreen.KEY_APPOINTMENT_UUID, appointmentUuid)
            .putExtra(KEY_PATIENT_UUID, patientUuid)
  }

  @Inject
  lateinit var controller: RemoveAppointmentScreenController

  private val patientAlreadyVisitedRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_patient_already_visited)
  private val notRespondingRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_patient_not_responding)
  private val invalidPhoneNumberRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_invalid_phone_number)
  private val publicHospitalTransferRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_public_hospital_transfer)
  private val movedToPrivateRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_moved_to_private)
  private val diedRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_patient_died)
  private val otherReasonRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_other)
  private val reasonSelectedDoneButton by bindView<View>(R.id.removeappointment_done_button)
  private val toolbar by bindView<Toolbar>(R.id.removeappointment_toolbar)

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.screen_remove_appointment)
    TheActivity.component.inject(this)

    bindUiToController(
        ui = this,
        events = Observable.mergeArray(
            sheetCreates(),
            cancelReasonClicks(),
            doneClicks(),
            patientDiedClicks(),
            patientAlreadyVisitedClicks()
        ),
        controller = controller,
        screenDestroys = onDestroys
    )

    toolbar.setNavigationOnClickListener { closeScreen() }
  }

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
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

  private fun patientDiedClicks() =
      RxView
          .clicks(diedRadioButton)
          .map { PatientDeadClicked(patientUuid = intent.extras!!.getSerializable(KEY_PATIENT_UUID) as UUID) }

  private fun patientAlreadyVisitedClicks() =
      RxView
          .clicks(patientAlreadyVisitedRadioButton)
          .map { PatientAlreadyVisitedClicked }

  private fun cancelReasonClicks(): Observable<UiEvent> {
    val buttonToCancelReasons = mapOf(
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

  fun closeScreen() {
    finish()
  }

  fun enableDoneButton() {
    reasonSelectedDoneButton.isEnabled = true
  }
}
