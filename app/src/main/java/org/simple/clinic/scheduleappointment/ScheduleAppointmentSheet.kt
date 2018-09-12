package org.simple.clinic.scheduleappointment

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
import org.threeten.bp.temporal.ChronoUnit
import javax.inject.Inject

class ScheduleAppointmentSheet : BottomSheetActivity() {

  private val possibleDates = listOf(
      ("1 day" to (1 to ChronoUnit.DAYS)),
      ("2 days" to (2 to ChronoUnit.DAYS)),
      ("3 days" to (3 to ChronoUnit.DAYS)),
      ("4 days" to (4 to ChronoUnit.DAYS)),
      ("5 days" to (5 to ChronoUnit.DAYS)),
      ("6 days" to (6 to ChronoUnit.DAYS)),
      ("7 days" to (7 to ChronoUnit.DAYS)),
      ("2 weeks" to (2 to ChronoUnit.WEEKS)),
      ("3 weeks" to (3 to ChronoUnit.WEEKS)),
      ("4 weeks" to (4 to ChronoUnit.WEEKS)),
      ("5 weeks" to (5 to ChronoUnit.WEEKS)),
      ("6 weeks" to (6 to ChronoUnit.WEEKS)),
      ("7 weeks" to (7 to ChronoUnit.WEEKS)),
      ("2 months" to (2 to ChronoUnit.MONTHS)),
      ("3 months" to (3 to ChronoUnit.MONTHS)),
      ("4 months" to (4 to ChronoUnit.MONTHS)),
      ("5 months" to (5 to ChronoUnit.MONTHS)),
      ("6 months" to (6 to ChronoUnit.MONTHS)),
      ("7 months" to (7 to ChronoUnit.MONTHS)),
      ("8 months" to (8 to ChronoUnit.MONTHS)),
      ("9 months" to (9 to ChronoUnit.MONTHS)),
      ("10 months" to (10 to ChronoUnit.MONTHS)),
      ("11 months" to (11 to ChronoUnit.MONTHS)),
      ("12 months" to (12 to ChronoUnit.MONTHS))
  )

  @Inject
  lateinit var controller: ScheduleAppointmentSheetController

  private var currentState = 0

  private val decrementDateButton by bindView<ImageButton>(R.id.scheduleappointment_decrement_date)
  private val incrementDateButton by bindView<ImageButton>(R.id.scheduleappointment_increment_date)
  private val currentDateTextView by bindView<TextView>(R.id.scheduleappointment_current_date)
  private val skipButton by bindView<Button>(R.id.scheduleappointment_skip)
  private val doneButton by bindView<Button>(R.id.scheduleappointment_done)

  private val onDestroys = PublishSubject.create<Any>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_schedule_appointment)
    TheActivity.component.inject(this)

    Observable.merge(decrementClicks(), incrementClicks(), skipClicks(), doneClicks())
        .startWith(SheetCreated(initialState = currentState))
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

  private fun incrementClicks() = RxView.clicks(incrementDateButton).map { IncrementAppointmentDate(currentState, possibleDates.size) }

  private fun decrementClicks() = RxView.clicks(decrementDateButton).map { DecrementAppointmentDate(currentState) }

  private fun skipClicks() = RxView.clicks(skipButton).map { SkipScheduling() }

  private fun doneClicks() = RxView.clicks(doneButton).map { ScheduleAppointment(possibleDates[currentState]) }

  fun closeSheet() {
    finish()
  }

  fun updateDisplayedDate(newState: Int) {
    currentState = newState
    currentDateTextView.text = possibleDates[currentState].first
  }

  fun enableIncrementButton(state: Boolean) {
    incrementDateButton.isEnabled = state
  }

  fun enableDecrementButton(state: Boolean) {
    decrementDateButton.isEnabled = state
  }
}
