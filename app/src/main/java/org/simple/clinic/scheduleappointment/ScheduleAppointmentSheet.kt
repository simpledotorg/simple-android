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
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit.DAYS
import org.threeten.bp.temporal.ChronoUnit.MONTHS
import java.util.UUID
import javax.inject.Inject

class ScheduleAppointmentSheet : BottomSheetActivity() {

  companion object {
    private const val KEY_PATIENT_UUID = "patientUuid"

    fun intent(context: Context, patientUuid: UUID): Intent =
        Intent(context, ScheduleAppointmentSheet::class.java)
            .putExtra(KEY_PATIENT_UUID, patientUuid)
  }

  private val oneMonth = ScheduleAppointment.DEFAULT

  private val possibleDates = listOf(
      ScheduleAppointment("1 day", 1, DAYS),
      ScheduleAppointment("2 days", 2, DAYS),
      ScheduleAppointment("3 days", 3, DAYS),
      ScheduleAppointment("4 days", 4, DAYS),
      ScheduleAppointment("5 days", 5, DAYS),
      ScheduleAppointment("6 days", 6, DAYS),
      ScheduleAppointment("7 days", 7, DAYS),
      ScheduleAppointment("8 days", 8, DAYS),
      ScheduleAppointment("9 days", 9, DAYS),
      ScheduleAppointment("10 days", 10, DAYS),
      ScheduleAppointment("20 days", 20, DAYS),
      oneMonth,
      ScheduleAppointment("2 months", 2, MONTHS),
      ScheduleAppointment("3 months", 3, MONTHS),
      ScheduleAppointment("4 months", 4, MONTHS),
      ScheduleAppointment("5 months", 5, MONTHS),
      ScheduleAppointment("6 months", 6, MONTHS),
      ScheduleAppointment("7 months", 7, MONTHS),
      ScheduleAppointment("8 months", 8, MONTHS),
      ScheduleAppointment("9 months", 9, MONTHS),
      ScheduleAppointment("10 months", 10, MONTHS),
      ScheduleAppointment("11 months", 11, MONTHS),
      ScheduleAppointment("12 months", 12, MONTHS)
  )

  @Inject
  lateinit var controller: ScheduleAppointmentSheetController

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var dateFormatter: DateTimeFormatter

  private var currentIndex = 0

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
    return Observable.just(ScheduleAppointmentSheetCreated2(
        possibleAppointments = possibleDates,
        defaultAppointment = oneMonth,
        patientUuid = patientUuid
    ))
  }

  private fun incrementClicks() = RxView.clicks(incrementDateButton).map { AppointmentDateIncremented2 }

  private fun decrementClicks() = RxView.clicks(decrementDateButton).map { AppointmentDateDecremented2 }

  private fun notNowClicks() = RxView.clicks(notNowButton).map { SchedulingSkipped() }

  private fun doneClicks() = RxView.clicks(doneButton).map { AppointmentDone }

  private fun chooseCalendarClicks() = RxView.clicks(calendarButton).map { AppointmentChooseCalendarClicks }

  fun closeSheet() {
    setResult(Activity.RESULT_OK)
    finish()
  }

  fun updateDisplayedDate(newIndex: Int) {
    currentIndex = newIndex
    currentDateTextView.text = possibleDates[currentIndex].displayText
  }

  @SuppressLint("SetTextI18n")
  fun updateDisplayedDate(year: Int, month: Int, dayOfMonth: Int) {
    calendarButton.text = "$year, $month, $dayOfMonth"
  }

  @SuppressLint("SetTextI18n")
  fun updateScheduledAppointment(appointment: ScheduleAppointment) {
    val localDate = LocalDate.now(utcClock).plus(appointment.timeAmount.toLong(), appointment.chronoUnit)
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
    DatePickerDialog(
        this, { _, year, month, dayOfMonth ->
      calendarDateSelectedEvents.onNext(AppointmentCalendarDateSelected(
          year = year,
          month = month + 1,
          dayOfMonth = dayOfMonth
      ))
    }, date.year, date.monthValue - 1, date.dayOfMonth).show()
  }
}
