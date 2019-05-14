package org.simple.clinic.home.overdue.appointmentreminder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToControllerWithoutDelay
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

class AppointmentReminderSheet : BottomSheetActivity() {

  companion object {
    private const val KEY_APPOINTMENT_UUID = "KEY_APPOINTMENT_UUID"

    fun intent(context: Context, appointmentUuid: UUID) =
        Intent(context, AppointmentReminderSheet::class.java)
            .putExtra(AppointmentReminderSheet.KEY_APPOINTMENT_UUID, appointmentUuid)!!
  }

  private val possibleDates = listOf(
      AppointmentReminder("1 day", 1, ChronoUnit.DAYS),
      AppointmentReminder("2 days", 2, ChronoUnit.DAYS),
      AppointmentReminder("3 days", 3, ChronoUnit.DAYS),
      AppointmentReminder("4 days", 4, ChronoUnit.DAYS),
      AppointmentReminder("5 days", 5, ChronoUnit.DAYS),
      AppointmentReminder("6 days", 6, ChronoUnit.DAYS),
      AppointmentReminder("7 days", 7, ChronoUnit.DAYS),
      AppointmentReminder("2 weeks", 2, ChronoUnit.WEEKS),
      AppointmentReminder("3 weeks", 3, ChronoUnit.WEEKS),
      AppointmentReminder("4 weeks", 4, ChronoUnit.WEEKS),
      AppointmentReminder("5 weeks", 5, ChronoUnit.WEEKS),
      AppointmentReminder("6 weeks", 6, ChronoUnit.WEEKS),
      AppointmentReminder("7 weeks", 7, ChronoUnit.WEEKS),
      AppointmentReminder("8 weeks", 8, ChronoUnit.WEEKS),
      AppointmentReminder("9 weeks", 9, ChronoUnit.WEEKS),
      AppointmentReminder("10 weeks", 10, ChronoUnit.WEEKS),
      AppointmentReminder("11 weeks", 11, ChronoUnit.WEEKS),
      AppointmentReminder("12 weeks", 12, ChronoUnit.WEEKS)
  )

  @Inject
  lateinit var controller: AppointmentReminderSheetController

  private var currentIndex = 0

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()

  private val decrementDateButton by bindView<ImageButton>(R.id.appointmentreminder_decrement_date)
  private val incrementDateButton by bindView<ImageButton>(R.id.appointmentreminder_increment_date)
  private val currentDateTextView by bindView<TextView>(R.id.appointmentreminder_current_date)
  private val doneButton by bindView<Button>(R.id.appointmentreminder_done)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_appointment_reminder)
    TheActivity.component.inject(this)

    bindUiToControllerWithoutDelay(
        ui = this,
        events = Observable.merge(
            sheetCreates(),
            incrementClicks(),
            decrementClicks(),
            doneClicks()
        ),
        controller = controller,
        screenDestroys = onDestroys
    )
  }

  private fun sheetCreates(): Observable<UiEvent> {
    val uuid = intent.extras.getSerializable(AppointmentReminderSheet.KEY_APPOINTMENT_UUID) as UUID
    return Observable.just(AppointmentReminderSheetCreated(initialIndex = 6, appointmentUuid = uuid))
  }

  private fun incrementClicks() = RxView.clicks(incrementDateButton).map { ReminderDateIncremented(currentIndex, possibleDates.size) }

  private fun decrementClicks() = RxView.clicks(decrementDateButton).map { ReminderDateDecremented(currentIndex, possibleDates.size) }

  private fun doneClicks() = RxView.clicks(doneButton).map { ReminderCreated(possibleDates[currentIndex]) }

  fun closeSheet() {
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

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }
}

