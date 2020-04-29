package org.simple.clinic.scheduleappointment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import com.jakewharton.rxbinding2.view.RxView
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.sheet_schedule_appointment.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.scheduleappointment.di.ScheduleAppointmentSheetComponent
import org.simple.clinic.scheduleappointment.facilityselection.FacilitySelectionActivity
import org.simple.clinic.scheduleappointment.facilityselection.FacilitySelectionActivity.Companion.selectedFacilityUuid
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.ThreeTenBpDatePickerDialog
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class ScheduleAppointmentSheet : BottomSheetActivity() {

  companion object {
    private const val REQCODE_FACILITY_SELECT = 100
    private const val KEY_PATIENT_UUID = "patientUuid"
    private const val KEY_EXTRA = "extra"

    fun intent(
        context: Context,
        patientUuid: UUID,
        extra: Parcelable?
    ): Intent {
      return Intent(context, ScheduleAppointmentSheet::class.java)
          .putExtra(KEY_PATIENT_UUID, patientUuid)
          .putExtra(KEY_EXTRA, extra)
    }

    fun <T : Parcelable> readExtra(intent: Intent): T? {
      return intent.getParcelableExtra(KEY_EXTRA) as T
    }
  }

  @Inject
  lateinit var controller: ScheduleAppointmentSheetController

  @Inject
  lateinit var userClock: UserClock

  @field:[Inject Named("full_date")]
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  lateinit var locale: Locale

  private lateinit var component: ScheduleAppointmentSheetComponent

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()
  private val calendarDateSelectedEvents: Subject<AppointmentCalendarDateSelected> = PublishSubject.create()
  private val facilityChanges: Subject<PatientFacilityChanged> = PublishSubject.create()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_schedule_appointment)

    bindUiToController(
        ui = this,
        events = Observable.mergeArray(
            screenCreates(),
            decrementClicks(),
            incrementClicks(),
            notNowClicks(),
            doneClicks(),
            appointmentDateClicks(),
            calendarDateSelectedEvents,
            facilityChanges
        ),
        controller = controller,
        screenDestroys = onDestroys
    )

    changeFacilityButton.setOnClickListener {
      openFacilitySelection()
    }
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
        .scheduleAppointmentSheetComponentBuilder()
        .activity(this)
        .build()

    component.inject(this)
  }

  override fun onDestroy() {
    onDestroys.onNext(ScreenDestroyed())
    super.onDestroy()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQCODE_FACILITY_SELECT && resultCode == Activity.RESULT_OK) {
      if (data != null) {
        facilityChanges.onNext(PatientFacilityChanged(selectedFacilityUuid(data!!)))
      }
    }
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

  private fun appointmentDateClicks() = RxView.clicks(changeAppointmentDate).map { ManuallySelectAppointmentDateClicked }

  private fun openFacilitySelection() {
    startActivityForResult(Intent(this, FacilitySelectionActivity::class.java), REQCODE_FACILITY_SELECT)
  }

  fun closeSheet() {
    val resultIntent = Intent().putExtra(KEY_EXTRA, intent.getParcelableExtra<Parcelable>(KEY_EXTRA))

    setResult(Activity.RESULT_OK, resultIntent)
    finish()
  }

  fun updateScheduledAppointment(appointmentDate: LocalDate, timeToAppointment: TimeToAppointment) {
    currentAppointmentDate.text = dateFormatter.format(appointmentDate)
    currentDateTextView.text = displayTextForTimeToAppointment(timeToAppointment)
  }

  private fun displayTextForTimeToAppointment(timeToAppointment: TimeToAppointment): String {
    val quantityStringResourceId = when (timeToAppointment) {
      is TimeToAppointment.Days -> R.plurals.scheduleappointment_appointment_in_days
      is TimeToAppointment.Weeks -> R.plurals.scheduleappointment_appointment_in_weeks
      is TimeToAppointment.Months -> R.plurals.scheduleappointment_appointment_in_months
    }

    return resources.getQuantityString(quantityStringResourceId, timeToAppointment.value, "${timeToAppointment.value}")
  }

  fun enableIncrementButton(state: Boolean) {
    incrementDateButton.isEnabled = state
  }

  fun enableDecrementButton(state: Boolean) {
    decrementDateButton.isEnabled = state
  }

  fun showManualDateSelector(date: LocalDate) {
    val today = LocalDate.now(userClock)

    ThreeTenBpDatePickerDialog(
        context = this,
        preselectedDate = date,
        allowedDateRange = today.plusDays(1)..today.plusYears(1),
        clock = userClock,
        datePickedListener = { pickedDate -> calendarDateSelectedEvents.onNext(AppointmentCalendarDateSelected(pickedDate)) }
    ).show()
  }

  fun showPatientFacility(facilityName: String) {
    selectedFacilityName.text = facilityName
  }
}
