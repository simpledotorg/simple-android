package org.simple.clinic.scheduleappointment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.SheetScheduleAppointmentBinding
import org.simple.clinic.datepicker.DatePickerKeyFactory
import org.simple.clinic.datepicker.SelectedDate
import org.simple.clinic.di.injector
import org.simple.clinic.facility.Facility
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.DeferredEventSource
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.newentry.ButtonState
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.scheduleappointment.facilityselection.FacilitySelectionScreen
import org.simple.clinic.summary.AppointmentSheetOpenedFrom
import org.simple.clinic.summary.teleconsultation.status.TeleconsultStatusSheet
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.setFragmentResultListener
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.Enabled
import org.simple.clinic.widgets.ProgressMaterialButton.ButtonState.InProgress
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named
import org.simple.clinic.scheduleappointment.ButtonState as NextButtonState

class ScheduleAppointmentSheet : BaseBottomSheet<
    ScheduleAppointmentSheet.Key,
    SheetScheduleAppointmentBinding,
    ScheduleAppointmentModel,
    ScheduleAppointmentEvent,
    ScheduleAppointmentEffect,
    ScheduleAppointmentViewEffect>(), ScheduleAppointmentUi, ScheduleAppointmentUiActions {

  companion object {
    private const val REQUEST_CODE_TELECONSULT_STATUS_CHANGED = 11

    fun sheetOpenedFrom(result: Succeeded): AppointmentSheetOpenedFrom {
      return result.result as AppointmentSheetOpenedFrom
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

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var datePickerKeyFactory: DatePickerKeyFactory

  private val changeFacilityButton
    get() = binding.changeFacilityButton

  private val incrementDateButton
    get() = binding.incrementDateButton

  private val decrementDateButton
    get() = binding.decrementDateButton

  private val notNowButton
    get() = binding.notNowButton

  private val doneButton
    get() = binding.doneButton

  private val nextButton
    get() = binding.nextButton

  private val changeAppointmentDate
    get() = binding.changeAppointmentDate

  private val currentAppointmentDate
    get() = binding.currentAppointmentDate

  private val currentDateTextView
    get() = binding.currentDateTextView

  private val selectedFacilityName
    get() = binding.selectedFacilityName

  private val calendarDateSelectedEvents: Subject<AppointmentCalendarDateSelected> = PublishSubject.create()
  private val facilityChanges: DeferredEventSource<ScheduleAppointmentEvent> = DeferredEventSource()

  override fun defaultModel() = ScheduleAppointmentModel.create(
      patientUuid = screenKey.patientId,
      timeToAppointments = config.scheduleAppointmentsIn,
      userClock = userClock,
      doneButtonState = ButtonState.SAVED,
      nextButtonState = NextButtonState.SCHEDULED
  )

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
      SheetScheduleAppointmentBinding.inflate(layoutInflater, container, false)

  override fun uiRenderer() = ScheduleAppointmentUiRenderer(this)

  override fun viewEffectsHandler() = ScheduleAppointmentViewEffectHandler(this)

  override fun events() = Observable
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
      .cast<ScheduleAppointmentEvent>()

  override fun createUpdate() = ScheduleAppointmentUpdate(
      currentDate = LocalDate.now(userClock),
      defaulterAppointmentPeriod = config.appointmentDuePeriodForDefaulters
  )

  override fun createInit() = ScheduleAppointmentInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<ScheduleAppointmentViewEffect>) = effectHandlerFactory.create(
      viewEffectsConsumer = viewEffectsConsumer
  ).build()

  override fun additionalEventSources() = listOf(facilityChanges)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    changeFacilityButton.setOnClickListener {
      openFacilitySelection()
    }

    setFragmentResultListener(Request.PickAppointmentDate, Request.SelectFacility) { requestKey, result ->
      if(result is Succeeded) {
        handleSuccessfulScreenResult(requestKey, result)
      }
    }
  }

  private fun handleSuccessfulScreenResult(requestKey: Parcelable, result: Succeeded) {
    when (requestKey) {
      is Request.PickAppointmentDate -> {
        val selectedDate = result.result as SelectedDate
        val event = AppointmentCalendarDateSelected(selectedDate = selectedDate.date)
        calendarDateSelectedEvents.onNext(event)
      }
      is Request.SelectFacility -> {
        val selectedFacility = (result.result as FacilitySelectionScreen.SelectedFacility).facility
        notifyPatientFacilityChanged(selectedFacility)
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == Activity.RESULT_OK) {
      manageRequestCodes(requestCode, data)
    }
  }

  private fun manageRequestCodes(requestCode: Int, data: Intent?) {
    when (requestCode) {
      REQUEST_CODE_TELECONSULT_STATUS_CHANGED -> closeSheet()
    }
  }

  private fun notifyPatientFacilityChanged(selectedFacility: Facility) {
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
    router.pushExpectingResult(Request.SelectFacility, FacilitySelectionScreen.Key())
  }

  override fun closeSheet() {
    router.popWithResult(Succeeded(screenKey.sheetOpenedFrom))
  }

  override fun openTeleconsultStatusSheet(teleconsultRecordUuid: UUID) {
    startActivityForResult(
        TeleconsultStatusSheet.intent(
            context = requireContext(),
            teleconsultRecordId = teleconsultRecordUuid
        ),
        REQUEST_CODE_TELECONSULT_STATUS_CHANGED
    )
  }

  override fun updateScheduledAppointment(
      appointmentDate: LocalDate,
      timeToAppointment: TimeToAppointment
  ) {
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

    val key = datePickerKeyFactory.key(
        preselectedDate = date,
        allowedDateRange = today.plusDays(1)..today.plusYears(1)
    )

    router.pushExpectingResult(Request.PickAppointmentDate, key)
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

  @Parcelize
  data class Key(
      val patientId: UUID,
      val sheetOpenedFrom: AppointmentSheetOpenedFrom
  ) : ScreenKey() {

    override val analyticsName = "Schedule Appointment Sheet"

    override val type = ScreenType.Modal

    override fun instantiateFragment() = ScheduleAppointmentSheet()
  }

  sealed class Request : Parcelable {

    @Parcelize
    object SelectFacility : Request()

    @Parcelize
    object PickAppointmentDate: Request()
  }

  interface Injector {

    fun inject(target: ScheduleAppointmentSheet)
  }
}
