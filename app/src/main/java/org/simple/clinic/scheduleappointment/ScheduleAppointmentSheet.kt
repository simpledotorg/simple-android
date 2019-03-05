package org.simple.clinic.scheduleappointment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.widgets.BottomSheetActivity
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

class ScheduleAppointmentSheet : BottomSheetActivity() {

  companion object {
    private const val KEY_PATIENT_UUID = "patientUuid"
    private const val EXTRAS_SAVED_APPOINTMENT_DATE = "extras_saved_appointment_date"

    fun intent(context: Context, patientUuid: UUID) =
        Intent(context, ScheduleAppointmentSheet::class.java)
            .putExtra(KEY_PATIENT_UUID, patientUuid)!!

    fun appointmentSavedDate(intent: Intent): LocalDate? {
      return intent.extras!!.getSerializable(EXTRAS_SAVED_APPOINTMENT_DATE) as LocalDate
    }
  }

  private val possibleDates = listOf(
      ScheduleAppointment("1 day", 1, ChronoUnit.DAYS),
      ScheduleAppointment("2 days", 2, ChronoUnit.DAYS),
      ScheduleAppointment("3 days", 3, ChronoUnit.DAYS),
      ScheduleAppointment("4 days", 4, ChronoUnit.DAYS),
      ScheduleAppointment("5 days", 5, ChronoUnit.DAYS),
      ScheduleAppointment("6 days", 6, ChronoUnit.DAYS),
      ScheduleAppointment("7 days", 7, ChronoUnit.DAYS),
      ScheduleAppointment("8 days", 8, ChronoUnit.DAYS),
      ScheduleAppointment("9 days", 9, ChronoUnit.DAYS),
      ScheduleAppointment("10 days", 10, ChronoUnit.DAYS),
      ScheduleAppointment("20 days", 20, ChronoUnit.DAYS),
      ScheduleAppointment("1 month", 1, ChronoUnit.MONTHS),
      ScheduleAppointment("2 months", 2, ChronoUnit.MONTHS),
      ScheduleAppointment("3 months", 3, ChronoUnit.MONTHS),
      ScheduleAppointment("4 months", 4, ChronoUnit.MONTHS),
      ScheduleAppointment("5 months", 5, ChronoUnit.MONTHS),
      ScheduleAppointment("6 months", 6, ChronoUnit.MONTHS),
      ScheduleAppointment("7 months", 7, ChronoUnit.MONTHS),
      ScheduleAppointment("8 months", 8, ChronoUnit.MONTHS),
      ScheduleAppointment("9 months", 9, ChronoUnit.MONTHS),
      ScheduleAppointment("10 months", 10, ChronoUnit.MONTHS),
      ScheduleAppointment("11 months", 11, ChronoUnit.MONTHS),
      ScheduleAppointment("12 months", 12, ChronoUnit.MONTHS)
  )

  @Inject
  lateinit var controller: ScheduleAppointmentSheetController

  private var currentIndex = 0

  private val decrementDateButton by bindView<ImageButton>(R.id.scheduleappointment_decrement_date)
  private val incrementDateButton by bindView<ImageButton>(R.id.scheduleappointment_increment_date)
  private val currentDateTextView by bindView<TextView>(R.id.scheduleappointment_current_date)
  private val notNowButton by bindView<Button>(R.id.scheduleappointment_not_now)
  private val doneButton by bindView<Button>(R.id.scheduleappointment_done)

  private val onDestroys = PublishSubject.create<Any>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_schedule_appointment)
    TheActivity.component.inject(this)

    Observable.merge(decrementClicks(), incrementClicks(), notNowClicks(), doneClicks())
        .startWith(initialState())
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

  private val initialState = {
    val uuid = intent.extras.getSerializable(KEY_PATIENT_UUID) as UUID
    ScheduleAppointmentSheetCreated(
        defaultDateIndex = possibleDates.lastIndex,
        patientUuid = uuid,
        numberOfDates = possibleDates.size
    )
  }

  private fun incrementClicks() = RxView.clicks(incrementDateButton).map { AppointmentDateIncremented(currentIndex, possibleDates.size) }

  private fun decrementClicks() = RxView.clicks(decrementDateButton).map { AppointmentDateDecremented(currentIndex, possibleDates.size) }

  private fun notNowClicks() = RxView.clicks(notNowButton).map { SchedulingSkipped() }

  private fun doneClicks() = RxView.clicks(doneButton).map { AppointmentScheduled(possibleDates[currentIndex]) }

  fun closeSheet() {
    setResult(Activity.RESULT_OK)
    finish()
  }

  fun closeSheet(appointmentDate: LocalDate) {
    val intent = Intent()
    intent.putExtra(EXTRAS_SAVED_APPOINTMENT_DATE, appointmentDate)
    setResult(Activity.RESULT_OK, intent)
    finish()
  }

  fun updateDisplayedDate(newIndex: Int) {
    currentIndex = newIndex
    currentDateTextView.text = possibleDates[currentIndex].displayText
  }

  fun enableIncrementButton(state: Boolean) {
    incrementDateButton.isEnabled = state
  }

  fun enableDecrementButton(state: Boolean) {
    decrementDateButton.isEnabled = state
  }
}
