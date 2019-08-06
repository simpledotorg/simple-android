package org.simple.clinic.scheduleappointment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToControllerWithoutDelay
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toUtcInstant
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class ScheduleAppointmentSheet : BottomSheetActivity() {

  companion object {
    private const val KEY_PATIENT_UUID = "patientUuid"

    fun intent(context: Context, patientUuid: UUID): Intent =
        Intent(context, ScheduleAppointmentSheet::class.java)
            .putExtra(KEY_PATIENT_UUID, patientUuid)
  }

  @Inject
  lateinit var controller: ScheduleAppointmentSheetController

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var dateFormatter: DateTimeFormatter

  private val decrementDateButton by bindView<ImageButton>(R.id.scheduleappointment_decrement_date)
  private val incrementDateButton by bindView<ImageButton>(R.id.scheduleappointment_increment_date)
  private val calendarButton by bindView<Button>(R.id.scheduleappointment_calendar_button)
  private val currentDateTextView by bindView<TextView>(R.id.scheduleappointment_current_date)
  private val notNowButton by bindView<Button>(R.id.scheduleappointment_not_now)
  private val doneButton by bindView<Button>(R.id.scheduleappointment_done)

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()
  private val calendarDateSelectedEvents: Subject<AppointmentCalendarDateSelected> = PublishSubject.create()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_schedule_appointment)
    TheActivity.component.inject(this)

    bindUiToControllerWithoutDelay(
        ui = this,
        events = Observable.mergeArray(
            screenCreates(),
            decrementClicks(),
            incrementClicks(),
            notNowClicks(),
            doneClicks(),
            chooseCalendarClicks(),
            calendarDateSelectedEvents
        ),
        controller = controller,
        screenDestroys = onDestroys
    )
  }

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  private fun screenCreates(): Observable<UiEvent> {
    val patientUuid = intent.extras.getSerializable(KEY_PATIENT_UUID) as UUID
    return Observable.just(ScheduleAppointmentSheetCreated(
        patientUuid = patientUuid
    ))
  }

  private fun incrementClicks() = RxView.clicks(incrementDateButton).map { AppointmentDateIncremented }

  private fun decrementClicks() = RxView.clicks(decrementDateButton).map { AppointmentDateDecremented }

  private fun notNowClicks() = RxView.clicks(notNowButton).map { SchedulingSkipped }

  private fun doneClicks() = RxView.clicks(doneButton).map { AppointmentDone }

  private fun chooseCalendarClicks() = RxView.clicks(calendarButton).map { AppointmentChooseCalendarClicks }

  fun closeSheet() {
    setResult(Activity.RESULT_OK)
    finish()
  }

  @SuppressLint("SetTextI18n")
  fun updateScheduledAppointment(appointment: ScheduleAppointment) {
    val localDate = LocalDate.now(userClock).plus(appointment.timeAmount.toLong(), appointment.chronoUnit)
    calendarButton.text = dateFormatter.format(localDate)
    currentDateTextView.text = appointment.displayText
  }

  fun enableIncrementButton(state: Boolean) {
    incrementDateButton.isEnabled = state
  }

  fun enableDecrementButton(state: Boolean) {
    decrementDateButton.isEnabled = state
  }

  fun showCalendar(date: LocalDate) {
    val datePickerDialog = DatePickerDialog(
        this, { _, year, month, dayOfMonth ->
      calendarDateSelectedEvents.onNext(AppointmentCalendarDateSelected(
          year = year,
          month = month + 1,
          dayOfMonth = dayOfMonth
      ))
    }, date.year, date.monthValue - 1, date.dayOfMonth)

    datePickerDialog.datePicker.apply {
      minDate = System.currentTimeMillis()
      maxDate = LocalDate.now(userClock).plusYears(1).toUtcInstant(userClock).toEpochMilli()
    }
    datePickerDialog.show()
  }
}
