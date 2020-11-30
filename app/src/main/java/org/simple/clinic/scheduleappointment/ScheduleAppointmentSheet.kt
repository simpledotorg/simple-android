package org.simple.clinic.scheduleappointment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.view.View.GONE
import android.view.View.VISIBLE
import com.jakewharton.rxbinding3.view.clicks
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.sheet_schedule_appointment.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.DeferredEventSource
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.newentry.ButtonState
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.scheduleappointment.di.ScheduleAppointmentSheetComponent
import org.simple.clinic.scheduleappointment.facilityselection.FacilitySelectionActivity
import org.simple.clinic.summary.teleconsultation.status.TeleconsultStatusSheet
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.withLocale
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.Enabled
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.InProgress
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.ThreeTenBpDatePickerDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import org.simple.clinic.scheduleappointment.ButtonState as NextButtonState

class ScheduleAppointmentSheet : BottomSheetActivity(), ScheduleAppointmentUi, ScheduleAppointmentUiActions {

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
      return intent.getParcelableExtra<T>(KEY_EXTRA)!!
    }
  }

  @Inject
  lateinit var userClock: UserClock

  @Inject
  @Named("full_date")
  lateinit var dateFormatter: DateTimeFormatter

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var effectHandlerFactory: ScheduleAppointmentEffectHandler.Factory

  @Inject
  lateinit var config: AppointmentConfig

  @Inject
  lateinit var features: Features

  private lateinit var component: ScheduleAppointmentSheetComponent

  private val onDestroys = PublishSubject.create<ScreenDestroyed>()
  private val calendarDateSelectedEvents: Subject<AppointmentCalendarDateSelected> = PublishSubject.create()
  private val facilityChanges: DeferredEventSource<ScheduleAppointmentEvent> = DeferredEventSource()
  private val REQUEST_CODE_TELECONSULT_STATUS_CHANGED = 11

  private val events by unsafeLazy {
    Observable
        .mergeArray(
            decrementClicks(),
            incrementClicks(),
            notNowClicks(),
            doneClicks(),
            appointmentDateClicks(),
            nextClicks(),
            calendarDateSelectedEvents
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val patientUuid = intent.extras!!.getSerializable(KEY_PATIENT_UUID) as UUID

    val uiRenderer = ScheduleAppointmentUiRenderer(this)

    MobiusDelegate.forActivity(
        events = events.ofType(),
        defaultModel = ScheduleAppointmentModel.create(
            patientUuid = patientUuid,
            timeToAppointments = config.scheduleAppointmentsIn,
            userClock = userClock,
            doneButtonState = ButtonState.SAVED,
            nextButtonState = NextButtonState.SCHEDULED
        ),
        update = ScheduleAppointmentUpdate(
            currentDate = LocalDate.now(userClock),
            defaulterAppointmentPeriod = config.appointmentDuePeriodForDefaulters
        ),
        init = ScheduleAppointmentInit(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render,
        additionalEventSources = listOf(facilityChanges)
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sheet_schedule_appointment)
    delegate.onRestoreInstanceState(savedInstanceState)

    changeFacilityButton.setOnClickListener {
      openFacilitySelection()
    }
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    delegate.stop()
    super.onStop()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    delegate.onSaveInstanceState(outState)
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDiGraph()

    val wrappedContext = baseContext
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
    applyOverrideConfiguration(Configuration())
  }

  override fun applyOverrideConfiguration(overrideConfiguration: Configuration) {
    super.applyOverrideConfiguration(overrideConfiguration.withLocale(locale, features))
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
    if (resultCode == Activity.RESULT_OK) {
      manageRequestCodes(requestCode, data)
    }
  }

  private fun manageRequestCodes(requestCode: Int, data: Intent?) {
    when (requestCode) {
      REQCODE_FACILITY_SELECT -> updateFacilityChangeForPatient(data)
      REQUEST_CODE_TELECONSULT_STATUS_CHANGED -> closeSheet()
    }
  }

  private fun updateFacilityChangeForPatient(data: Intent?) {
    val selectedFacility = FacilitySelectionActivity.selectedFacility(data!!)
    val patientFacilityChanged = PatientFacilityChanged(facility = selectedFacility)
    facilityChanges.notify(patientFacilityChanged)
  }

  private fun incrementClicks() = incrementDateButton.clicks().map { AppointmentDateIncremented }

  private fun decrementClicks() = decrementDateButton.clicks().map { AppointmentDateDecremented }

  private fun notNowClicks() = notNowButton.clicks().map { SchedulingSkipped }

  private fun doneClicks() = doneButton.clicks().map { DoneClicked }

  private fun nextClicks() = nextButton.clicks().map { NextClicked }

  private fun appointmentDateClicks() = changeAppointmentDate.clicks().map { ManuallySelectAppointmentDateClicked }

  private fun openFacilitySelection() {
    startActivityForResult(Intent(this, FacilitySelectionActivity::class.java), REQCODE_FACILITY_SELECT)
  }

  override fun closeSheet() {
    val resultIntent = Intent().putExtra(KEY_EXTRA, intent.getParcelableExtra<Parcelable>(KEY_EXTRA))

    setResult(Activity.RESULT_OK, resultIntent)
    finish()
  }

  override fun openTeleconsultStatusSheet(teleconsultRecordUuid: UUID) {
    startActivityForResult(
        TeleconsultStatusSheet.intent(
            context = this,
            teleconsultRecordId = teleconsultRecordUuid
        ),
        REQUEST_CODE_TELECONSULT_STATUS_CHANGED
    )
  }

  override fun updateScheduledAppointment(appointmentDate: LocalDate, timeToAppointment: TimeToAppointment) {
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

  override fun enableIncrementButton(state: Boolean) {
    incrementDateButton.isEnabled = state
  }

  override fun enableDecrementButton(state: Boolean) {
    decrementDateButton.isEnabled = state
  }

  override fun showManualDateSelector(date: LocalDate) {
    val today = LocalDate.now(userClock)

    ThreeTenBpDatePickerDialog(
        context = this,
        preselectedDate = date,
        allowedDateRange = today.plusDays(1)..today.plusYears(1),
        clock = userClock,
        datePickedListener = { pickedDate -> calendarDateSelectedEvents.onNext(AppointmentCalendarDateSelected(pickedDate)) }
    ).show()
  }

  override fun showPatientFacility(facilityName: String) {
    selectedFacilityName.text = facilityName
  }

  override fun showProgress() {
    doneButton.setButtonState(InProgress)
  }

  override fun hideProgress() {
    doneButton.setButtonState(Enabled)
  }

  override fun showDoneButton() {
    doneButton.visibility = VISIBLE
  }

  override fun showNextButton() {
    nextButton.visibility = VISIBLE
  }

  override fun hideDoneButton() {
    doneButton.visibility = GONE
  }

  override fun hideNextButton() {
    nextButton.visibility = GONE
  }

  override fun showNextButtonProgress() {
    nextButton.setButtonState(InProgress)
  }

  override fun hideNextButtonProgress() {
    nextButton.setButtonState(Enabled)
  }
}
