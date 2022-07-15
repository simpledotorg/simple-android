package org.simple.clinic.contactpatient

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialSharedAxis
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.activity.permissions.ActivityPermissionResult
import org.simple.clinic.activity.permissions.RequestPermissions
import org.simple.clinic.activity.permissions.RuntimePermissions
import org.simple.clinic.databinding.SheetContactPatientBinding
import org.simple.clinic.datepicker.DatePickerKeyFactory
import org.simple.clinic.datepicker.DatePickerResult
import org.simple.clinic.datepicker.SelectedDate
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Feature.SecureCalling
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.phone.Dialer
import org.simple.clinic.phone.PhoneCaller
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.simple.clinic.removeoverdueappointment.RemoveOverdueAppointmentScreen
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.onBackPressed
import org.simple.clinic.util.setFragmentResultListener
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.valueOrEmpty
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class ContactPatientBottomSheet : BaseBottomSheet<
    ContactPatientBottomSheet.Key,
    SheetContactPatientBinding,
    ContactPatientModel,
    ContactPatientEvent,
    ContactPatientEffect,
    Unit>(), ContactPatientUi, ContactPatientUiActions {

  @Inject
  lateinit var phoneCaller: PhoneCaller

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var phoneMaskConfig: PhoneNumberMaskerConfig

  @Inject
  lateinit var effectHandlerFactory: ContactPatientEffectHandler.Factory

  @Inject
  lateinit var userClock: UserClock

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  @Inject
  lateinit var appointmentConfig: AppointmentConfig

  @Inject
  @Named("full_date")
  lateinit var dateTimeFormatter: DateTimeFormatter

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var datePickerKeyFactory: DatePickerKeyFactory

  private val patientUuid by unsafeLazy { screenKey.patientId }

  private val permissionResults: Subject<ActivityPermissionResult> = PublishSubject.create()

  private val hotEvents: PublishSubject<ContactPatientEvent> = PublishSubject.create()

  private val contentFlipper
    get() = binding.contentFlipper

  private val callPatientView
    get() = binding.callPatientView

  private val setAppointmentReminderView
    get() = binding.setAppointmentReminderView

  private val progressIndicator
    get() = binding.progressIndicator

  override fun defaultModel() = ContactPatientModel.create(
      patientUuid = patientUuid,
      appointmentConfig = appointmentConfig,
      userClock = userClock,
      mode = UiMode.CallPatient,
      secureCallFeatureEnabled = features.isEnabled(SecureCalling) && phoneMaskConfig.proxyPhoneNumber.isNotBlank()
  )

  override fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
      SheetContactPatientBinding.inflate(inflater, container, false)

  override fun uiRenderer() = ContactPatientUiRenderer(this, userClock)

  override fun events() = Observable
      .mergeArray(
          normalCallClicks(),
          secureCallClicks(),
          agreedToVisitClicks(),
          remindToCallLaterClicks(),
          nextReminderDateClicks(),
          previousReminderDateClicks(),
          appointmentDateClicks(),
          saveReminderDateClicks(),
          removeFromOverdueListClicks(),
          hotEvents
      )
      .compose(RequestPermissions<ContactPatientEvent>(runtimePermissions, permissionResults))
      .compose(ReportAnalyticsEvents())
      .cast<ContactPatientEvent>()

  override fun createUpdate() = ContactPatientUpdate(phoneMaskConfig)

  override fun createInit() = ContactPatientInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Unit>) = effectHandlerFactory.create(this)
      .build()

  override fun onAttach(context: Context) {
    super.onAttach(context)

    context.injector<Injector>().inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return super.onCreateDialog(savedInstanceState).apply {
      onBackPressed(::backPressed)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setFragmentResultListener(DatePickerResult, RemoveOverdueAppointmentResult) { requestKey, result ->
      if (result !is Succeeded) return@setFragmentResultListener

      when (requestKey) {
        DatePickerResult -> {
          val selectedDate = result.result as SelectedDate
          val event = ManualDateSelected(selectedDate = selectedDate.date, currentDate = LocalDate.now(userClock))
          hotEvents.onNext(event)
        }
        RemoveOverdueAppointmentResult -> {
          router.pop()
        }
      }
    }
  }

  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<out String>,
      grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    permissionResults.onNext(ActivityPermissionResult(requestCode))
  }

  override fun renderPatientDetails(patientDetails: PatientDetails) {
    callPatientView.renderPatientDetails(
        PatientDetails(
            patientDetails.name,
            patientDetails.gender,
            patientDetails.age,
            patientDetails.phoneNumber.valueOrEmpty(),
            patientDetails.patientAddress,
            patientDetails.registeredFacility,
            patientDetails.diagnosedWithDiabetes,
            patientDetails.diagnosedWithHypertension,
            patientDetails.lastVisited
        ),
        dateTimeFormatter,
        userClock
    )
  }

  override fun showSecureCallUi() {
    callPatientView.secureCallingSectionVisible = true
  }

  override fun hideSecureCallUi() {
    callPatientView.secureCallingSectionVisible = false
  }

  override fun showNormalCallButtonText() {
    callPatientView.normalCallButtonText = getString(R.string.contactpatient_call_normal)
  }

  override fun showCallButtonText() {
    callPatientView.normalCallButtonText = getString(R.string.contactpatient_call)
  }

  override fun showPatientWithNoPhoneNumberUi() {
    callPatientView.showPatientWithNoPhoneNumberLayout = true
  }

  override fun hidePatientWithNoPhoneNumberUi() {
    callPatientView.showPatientWithNoPhoneNumberLayout = false
  }

  override fun showPatientWithPhoneNumberUi() {
    callPatientView.showPatientWithPhoneNumberLayout = true
  }

  override fun hidePatientWithPhoneNumberUi() {
    callPatientView.showPatientWithPhoneNumberLayout = false
  }

  override fun setResultOfCallLabelText() {
    callPatientView.setResultOfCallLabelText = getString(R.string.contactpatient_result_of_call)
  }

  override fun setResultLabelText() {
    callPatientView.setResultOfCallLabelText = getString(R.string.contactpatient_result)
  }

  override fun setRegisterAtLabelText() {
    callPatientView.setRegisteredFacilityLabelText = getString(R.string.contactpatient_patient_registered_at)
  }

  override fun setTransferredFromLabelText() {
    callPatientView.setRegisteredFacilityLabelText = getString(R.string.contactpatient_patient_transfer_from)
  }

  override fun directlyCallPatient(patientPhoneNumber: String, dialer: Dialer) {
    phoneCaller.normalCall(patientPhoneNumber, dialer)
  }

  override fun maskedCallPatient(patientPhoneNumber: String, proxyNumber: String, dialer: Dialer) {
    phoneCaller.secureCall(
        visibleNumber = proxyNumber,
        hiddenNumber = patientPhoneNumber,
        dialer = dialer
    )
  }

  override fun closeSheet() {
    router.pop()
  }

  override fun renderSelectedAppointmentDate(
      selectedAppointmentReminderPeriod: TimeToAppointment,
      selectedDate: LocalDate
  ) {
    setAppointmentReminderView.renderSelectedAppointmentDate(selectedAppointmentReminderPeriod, selectedDate)
  }

  override fun showManualDatePicker(
      preselectedDate: LocalDate,
      dateBounds: ClosedRange<LocalDate>
  ) {
    val key = datePickerKeyFactory.key(
        preselectedDate = preselectedDate,
        allowedDateRange = dateBounds
    )

    router.pushExpectingResult(DatePickerResult, key)
  }

  override fun disablePreviousReminderDateStepper() {
    setAppointmentReminderView.disablePreviousReminderDateStepper()
  }

  override fun enablePreviousReminderDateStepper() {
    setAppointmentReminderView.enablePreviousReminderDateStepper()
  }

  override fun disableNextReminderDateStepper() {
    setAppointmentReminderView.disableNextReminderDateStepper()
  }

  override fun enableNextReminderDateStepper() {
    setAppointmentReminderView.enableNextReminderDateStepper()
  }

  override fun showCallResult() {
    callPatientView.callResultOutcomeViewVisible = true
  }

  override fun hideCallResult() {
    callPatientView.callResultOutcomeViewVisible = false
  }

  override fun setupAgreedToVisitCallResultOutcome() {
    callPatientView.callResultOutcomeText = getString(R.string.call_result_outcome_agreed_to_visit)
    callPatientView.setupCallResultViewForAgreedToVisit()
  }

  override fun setupRemindToCallLaterCallResultOutcome(appointmentReminderDate: LocalDate) {
    val formattedDate = dateTimeFormatter.format(appointmentReminderDate)
    callPatientView.callResultOutcomeText = resources.getString(
        R.string.call_result_outcome_remind_to_call_later,
        formattedDate
    )
    callPatientView.setupCallResultViewForRemindToCallLater()
  }

  override fun setupRemovedFromListCallResultOutcome(removeReasonStringRes: Int) {
    val removeReason = getString(removeReasonStringRes)
    callPatientView.callResultOutcomeText = getString(R.string.call_result_outcome_removed, removeReason)
    callPatientView.setupCallResultViewForRemovedFromList()
  }

  override fun setCallResultUpdatedAtDate(callResultUpdatedAt: LocalDate) {
    val formattedDate = dateTimeFormatter.format(callResultUpdatedAt)
    callPatientView.callResultLastUpdatedDate = getString(
        R.string.call_result_outcome_saved_at,
        formattedDate
    )
  }

  override fun switchToCallPatientView() {
    callPatientView.visibility = VISIBLE
    setAppointmentReminderView.visibility = GONE
  }

  override fun switchToSetAppointmentReminderView() {
    sharedAxis(contentFlipper)

    callPatientView.visibility = GONE
    setAppointmentReminderView.visibility = VISIBLE
  }

  override fun openRemoveOverdueAppointmentScreen(appointment: Appointment) {
    router.pushExpectingResult(
        RemoveOverdueAppointmentResult,
        RemoveOverdueAppointmentScreen.Key(appointment)
    )
  }

  override fun showPatientWithPhoneNumberCallResults() {
    callPatientView.showPatientWithCallResultLayout = true
  }

  override fun hidePatientWithPhoneNumberCallResults() {
    callPatientView.showPatientWithCallResultLayout = false
  }

  override fun showProgress() {
    progressIndicator.visibility = VISIBLE
  }

  override fun hideProgress() {
    progressIndicator.visibility = GONE
  }

  override fun showPatientWithNoPhoneNumberResults() {
    callPatientView.showPatientWithNoPhoneNumberResults = true
  }

  override fun showDeadPatientStatus() {
    callPatientView.showPatientDiedStatus = true
  }

  override fun hideDeadPatientStatus() {
    callPatientView.showPatientDiedStatus = false
  }

  private fun backPressed() {
    hotEvents.onNext(BackClicked)
  }

  private fun normalCallClicks(): Observable<ContactPatientEvent> {
    return Observable.create { emitter ->
      emitter.setCancellable { callPatientView.normalCallButtonClicked = null }

      callPatientView.normalCallButtonClicked = { emitter.onNext(NormalCallClicked()) }
    }
  }

  private fun secureCallClicks(): Observable<ContactPatientEvent> {
    return Observable.create { emitter ->
      emitter.setCancellable { callPatientView.secureCallButtonClicked = null }

      callPatientView.secureCallButtonClicked = { emitter.onNext(SecureCallClicked()) }
    }
  }

  private fun agreedToVisitClicks(): Observable<ContactPatientEvent> {
    return Observable.create { emitter ->
      emitter.setCancellable { callPatientView.agreedToVisitClicked = null }

      callPatientView.agreedToVisitClicked = { emitter.onNext(PatientAgreedToVisitClicked) }
    }
  }

  private fun remindToCallLaterClicks(): Observable<ContactPatientEvent> {
    return Observable.create { emitter ->
      emitter.setCancellable { callPatientView.remindToCallLaterClicked = null }

      callPatientView.remindToCallLaterClicked = { emitter.onNext(RemindToCallLaterClicked) }
    }
  }

  private fun nextReminderDateClicks(): Observable<ContactPatientEvent> {
    return Observable.create { emitter ->
      emitter.setCancellable { setAppointmentReminderView.incrementStepperClicked = null }

      setAppointmentReminderView.incrementStepperClicked = { emitter.onNext(NextReminderDateClicked) }
    }
  }

  private fun previousReminderDateClicks(): Observable<ContactPatientEvent> {
    return Observable.create { emitter ->
      emitter.setCancellable { setAppointmentReminderView.decrementStepperClicked = null }

      setAppointmentReminderView.decrementStepperClicked = { emitter.onNext(PreviousReminderDateClicked) }
    }
  }

  private fun appointmentDateClicks(): Observable<ContactPatientEvent> {
    return Observable.create { emitter ->
      emitter.setCancellable { setAppointmentReminderView.appointmentDateClicked = null }

      setAppointmentReminderView.appointmentDateClicked = { emitter.onNext(AppointmentDateClicked) }
    }
  }

  private fun saveReminderDateClicks(): Observable<ContactPatientEvent> {
    return Observable.create { emitter ->
      emitter.setCancellable { setAppointmentReminderView.doneClicked = null }

      setAppointmentReminderView.doneClicked = { emitter.onNext(SaveAppointmentReminderClicked) }
    }
  }

  private fun removeFromOverdueListClicks(): Observable<ContactPatientEvent> {
    return Observable.create { emitter ->
      emitter.setCancellable { callPatientView.removeFromOverdueListClicked = null }

      callPatientView.removeFromOverdueListClicked = { emitter.onNext(RemoveFromOverdueListClicked) }
    }
  }

  private fun sharedAxis(viewGroup: ViewGroup) {
    val sharedAxis = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
      duration = 150
    }
    TransitionManager.beginDelayedTransition(viewGroup, sharedAxis)
  }

  @Parcelize
  data class Key(val patientId: UUID) : ScreenKey() {

    override val analyticsName = "Contact Patient Bottom Sheet"

    override val type = ScreenType.Modal

    override fun instantiateFragment() = ContactPatientBottomSheet()
  }

  @Parcelize
  object RemoveOverdueAppointmentResult : Parcelable

  interface Injector {
    fun inject(target: ContactPatientBottomSheet)
  }
}
