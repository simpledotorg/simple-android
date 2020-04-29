package org.simple.clinic.home.overdue.appointmentreminder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.jakewharton.rxbinding2.view.RxView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotterknife.bindView
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.home.overdue.appointmentreminder.di.AppointmentReminderSheetComponent
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class AppointmentReminderSheet : BottomSheetActivity() {

  companion object {
    private const val KEY_APPOINTMENT_UUID = "KEY_APPOINTMENT_UUID"

    fun intent(context: Context, appointmentUuid: UUID) =
        Intent(context, AppointmentReminderSheet::class.java)
            .putExtra(KEY_APPOINTMENT_UUID, appointmentUuid)!!
  }

  private val possibleDates = listOf(
      AppointmentReminder(1, ChronoUnit.DAYS),
      AppointmentReminder(2, ChronoUnit.DAYS),
      AppointmentReminder(3, ChronoUnit.DAYS),
      AppointmentReminder(4, ChronoUnit.DAYS),
      AppointmentReminder(5, ChronoUnit.DAYS),
      AppointmentReminder(6, ChronoUnit.DAYS),
      AppointmentReminder(7, ChronoUnit.DAYS),
      AppointmentReminder(2, ChronoUnit.WEEKS),
      AppointmentReminder(3, ChronoUnit.WEEKS),
      AppointmentReminder(4, ChronoUnit.WEEKS),
      AppointmentReminder(5, ChronoUnit.WEEKS),
      AppointmentReminder(6, ChronoUnit.WEEKS),
      AppointmentReminder(7, ChronoUnit.WEEKS),
      AppointmentReminder(8, ChronoUnit.WEEKS),
      AppointmentReminder(9, ChronoUnit.WEEKS),
      AppointmentReminder(10, ChronoUnit.WEEKS),
      AppointmentReminder(11, ChronoUnit.WEEKS),
      AppointmentReminder(12, ChronoUnit.WEEKS)
  )

  @Inject
  lateinit var controller: AppointmentReminderSheetController

  @Inject
  lateinit var locale: Locale

  private lateinit var component: AppointmentReminderSheetComponent

  private var currentIndex = 0

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()

  private val decrementDateButton by bindView<ImageButton>(R.id.appointmentreminder_decrement_date)
  private val incrementDateButton by bindView<ImageButton>(R.id.appointmentreminder_increment_date)
  private val currentDateTextView by bindView<TextView>(R.id.appointmentreminder_current_date)
  private val doneButton by bindView<MaterialButton>(R.id.appointmentreminder_done)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_appointment_reminder)

    bindUiToController(
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

  override fun attachBaseContext(baseContext: Context) {
    setupDiGraph()

    val wrappedContext = baseContext
        .wrap { LocaleOverrideContextWrapper.wrap(it, locale) }
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
  }

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .appointmentReminderSheetComponent()
        .activity(this)
        .build()

    component.inject(this)
  }

  private fun sheetCreates(): Observable<UiEvent> {
    val uuid = intent.extras!!.getSerializable(KEY_APPOINTMENT_UUID) as UUID
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
    val appointmentReminder = possibleDates[currentIndex]
    currentDateTextView.text = textForAppointmentReminder(appointmentReminder.timeAmount, appointmentReminder.chronoUnit)
  }

  fun enableIncrementButton(state: Boolean) {
    incrementDateButton.isEnabled = state
  }

  fun enableDecrementButton(state: Boolean) {
    decrementDateButton.isEnabled = state
  }

  private fun textForAppointmentReminder(timeAmount: Int, chronoUnit: ChronoUnit): String {
    val quantityStringResourceId = when (chronoUnit) {
      ChronoUnit.DAYS -> R.plurals.appointmentreminder_days
      ChronoUnit.WEEKS -> R.plurals.appointmentreminder_weeks
      else -> throw IllegalArgumentException("$chronoUnit is unsupported type for appointment reminders")
    }
    return resources.getQuantityString(quantityStringResourceId, timeAmount, timeAmount.toString())
  }

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }
}

