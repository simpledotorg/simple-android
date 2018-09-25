package org.simple.clinic.home.overdue.removepatient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.overdue.Appointment.CancelReason.DEAD
import org.simple.clinic.overdue.Appointment.CancelReason.MOVED
import org.simple.clinic.overdue.Appointment.CancelReason.OTHER
import org.simple.clinic.overdue.Appointment.CancelReason.PATIENT_NOT_RESPONDING
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.PrimarySolidButton
import org.simple.clinic.widgets.UiEvent
import java.util.UUID
import javax.inject.Inject

class RemoveAppointmentSheet : BottomSheetActivity() {

  companion object {
    private const val KEY_APPOINTMENT_UUID = "KEY_APPOINTMENT_UUID"

    fun intent(context: Context, appointmentUuid: UUID) =
        Intent(context, RemoveAppointmentSheet::class.java)
            .putExtra(RemoveAppointmentSheet.KEY_APPOINTMENT_UUID, appointmentUuid)!!
  }

  @Inject
  lateinit var controller: RemoveAppointmentSheetController

  private val alreadyVisitedRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_patient_already_visited)
  private val movedOutRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_patient_moved)
  private val notRespondingRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_patient_not_responding)
  private val diedRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_patient_died)
  private val otherReasonRadioButton by bindView<RadioButton>(R.id.removeappointment_reason_other)
  private val reasonSelectedDoneButton by bindView<PrimarySolidButton>(R.id.removeappointment_done_button)

  private val onDestroys = PublishSubject.create<Any>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_remove_appointment)
    TheActivity.component.inject(this)

    Observable.merge(sheetCreates(), alreadyVisitedClicks(), cancelReasonClicks(), doneClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(onDestroys)
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun sheetCreates(): Observable<UiEvent> {
    val appointmentUuid = intent.extras.getSerializable(KEY_APPOINTMENT_UUID) as UUID
    return Observable.just(RemoveAppointmentSheetCreated(appointmentUuid))
  }

  private fun doneClicks(): Observable<UiEvent> = RxView.clicks(reasonSelectedDoneButton).map { RemoveReasonDoneClicked }

  private fun alreadyVisitedClicks() = RxView.clicks(alreadyVisitedRadioButton).map { AlreadyVisitedReasonClicked }

  private fun cancelReasonClicks(): Observable<UiEvent> {
    val movedOutStream = RxView.clicks(movedOutRadioButton).map { CancelReasonClicked(MOVED) }
    val notRespondingStream = RxView.clicks(notRespondingRadioButton).map { CancelReasonClicked(PATIENT_NOT_RESPONDING) }
    val deathStream = RxView.clicks(diedRadioButton).map { CancelReasonClicked(DEAD) }
    val otherStream = RxView.clicks(otherReasonRadioButton).map { CancelReasonClicked(OTHER) }

    return Observable.merge(movedOutStream, notRespondingStream, deathStream, otherStream)
  }

  fun closeSheet() = finish()

  fun enableDoneButton() {
    reasonSelectedDoneButton.isEnabled = true
  }

  override fun onDestroy() {
    onDestroys.onNext(Any())
    super.onDestroy()
  }
}
