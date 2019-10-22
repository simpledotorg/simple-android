package org.simple.clinic.scheduleappointment

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.sheet_schedule_appointment.*
import org.simple.clinic.R
import org.simple.clinic.main.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toUtcInstant
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.Period
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

private typealias DatePickerDialogListener = (view: DatePicker, year: Int, month: Int, dayOfMonth: Int) -> Unit

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

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()
  private val calendarDateSelectedEvents: Subject<AppointmentCalendarDateSelected> = PublishSubject.create()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_schedule_appointment)
    TheActivity.component.inject(this)

    bindUiToController(
        ui = this,
        events = Observable.mergeArray(
            screenCreates(),
            decrementClicks(),
            incrementClicks(),
            notNowClicks(),
            doneClicks(),
            appointmentDateClicks(),
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
    val patientUuid = intent.extras!!.getSerializable(KEY_PATIENT_UUID) as UUID
    return Observable.just(ScheduleAppointmentSheetCreated(
        patientUuid = patientUuid
    ))
  }

  private fun incrementClicks() = RxView.clicks(incrementDateButton).map { AppointmentDateIncremented }

  private fun decrementClicks() = RxView.clicks(decrementDateButton).map { AppointmentDateDecremented }

  private fun notNowClicks() = RxView.clicks(notNowButton).map { SchedulingSkipped }

  private fun doneClicks() = RxView.clicks(doneButton).map { AppointmentDone }

  private fun appointmentDateClicks() = RxView.clicks(currentAppointmentDate).map { ManuallySelectAppointmentDateClicked }

  fun closeSheet() {
    setResult(Activity.RESULT_OK)
    finish()
  }

  fun updateScheduledAppointment(appointmentDate: LocalDate, timeToAppointment: TimeToAppointment) {
    currentAppointmentDate.text = dateFormatter.format(appointmentDate)
    currentDateTextView.text = displayTextForTimeToAppointment(timeToAppointment)
  }

  private fun displayTextForTimeToAppointment(timeToAppointment: TimeToAppointment): String {
    val quantityStringResourceId = when (timeToAppointment) {
      is TimeToAppointment.Days -> R.plurals.scheduleappointment_appointmentin_days
      is TimeToAppointment.Weeks -> R.plurals.scheduleappointment_appointmentin_weeks
      is TimeToAppointment.Months -> R.plurals.scheduleappointment_appointmentin_months
    }

    return resources.getQuantityString(quantityStringResourceId, timeToAppointment.value, timeToAppointment.value)
  }

  fun enableIncrementButton(state: Boolean) {
    incrementDateButton.isEnabled = state
  }

  fun enableDecrementButton(state: Boolean) {
    decrementDateButton.isEnabled = state
  }

  fun showManualDateSelector(date: LocalDate) {
    /*
     * The DatePickerDialog uses 0-based indices for the Month (0 is January, 1 is February...),
     * while LocalDate uses 1-based indices (1 is January, 2 is February...).
     *
     * So when we convert from LocalDate to the DatePicker (and vice versa), we have to adjust the
     * month accordingly.
     */
    val listener: DatePickerDialogListener = { _, year, month, dayOfMonth ->
      val selectedAppointmentDate = LocalDate.of(year, month + 1, dayOfMonth)
      calendarDateSelectedEvents.onNext(AppointmentCalendarDateSelected(selectedAppointmentDate))
    }
    val datePickerDialog = DatePickerDialog(this, listener, date.year, date.monthValue - 1, date.dayOfMonth)

    datePickerDialog.datePicker.apply {
      minDate = epochMillisForTimeInFuture(Period.ofDays(1))
      maxDate = epochMillisForTimeInFuture(Period.ofYears(1))
    }
    datePickerDialog.show()
  }

  private fun epochMillisForTimeInFuture(period: Period): Long {
    return LocalDate
        .now(userClock)
        .plus(period)
        .toUtcInstant(userClock)
        .toEpochMilli()
  }
}
