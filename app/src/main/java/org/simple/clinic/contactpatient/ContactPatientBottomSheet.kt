package org.simple.clinic.contactpatient

import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.SheetContactPatientBinding
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Feature.SecureCalling
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseBottomSheet
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.patient.Gender
import org.simple.clinic.phone.Dialer
import org.simple.clinic.phone.PhoneCaller
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.util.RequestPermissions
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ThreeTenBpDatePickerDialog
import java.time.LocalDate
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class ContactPatientBottomSheet : BaseBottomSheet<
    ContactPatientBottomSheet.Key,
    SheetContactPatientBinding,
    ContactPatientModel,
    ContactPatientEvent,
    ContactPatientEffect>(), ContactPatientUi, ContactPatientUiActions {

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
  lateinit var features: Features

  @Inject
  lateinit var router: Router

  private val patientUuid by unsafeLazy { screenKey.patientId }

  private val permissionResults: Subject<ActivityPermissionResult> = PublishSubject.create()

  private val hotEvents: PublishSubject<ContactPatientEvent> = PublishSubject.create()

  private val callPatientView
    get() = binding.callPatientView

  private val setAppointmentReminderView
    get() = binding.setAppointmentReminderView

  private val removeAppointmentView
    get() = binding.removeAppointmentView

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
          removeAppointmentCloseClicks(),
          removeAppointmentDoneClicks(),
          removeAppointmentReasonSelections(),
          hotEvents
      )
      .compose(RequestPermissions<ContactPatientEvent>(runtimePermissions, permissionResults))
      .compose(ReportAnalyticsEvents())
      .cast<ContactPatientEvent>()

  override fun createUpdate() = ContactPatientUpdate(phoneMaskConfig)

  override fun createInit() = ContactPatientInit()

  override fun createEffectHandler() = effectHandlerFactory.create(this)
      .build()

  override fun onAttach(context: Context) {
    super.onAttach(context)

    context.injector<Injector>().inject(this)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    permissionResults.onNext(ActivityPermissionResult(requestCode))
  }

  override fun renderPatientDetails(name: String, gender: Gender, age: Int, phoneNumber: String) {
    callPatientView.renderPatientDetails(name, gender, age, phoneNumber)
  }

  override fun showCallResultSection() {
    callPatientView.callResultSectionVisible = true
  }

  override fun hideCallResultSection() {
    callPatientView.callResultSectionVisible = false
  }

  override fun showSecureCallUi() {
    callPatientView.secureCallingSectionVisible = true
  }

  override fun hideSecureCallUi() {
    callPatientView.secureCallingSectionVisible = false
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
    ThreeTenBpDatePickerDialog(
        context = requireContext(),
        preselectedDate = preselectedDate,
        allowedDateRange = dateBounds,
        clock = userClock,
        datePickedListener = { pickedDate ->
          val event = ManualDateSelected(selectedDate = pickedDate, currentDate = LocalDate.now(userClock))
          hotEvents.onNext(event)
        }
    ).show()
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

  override fun switchToCallPatientView() {
    callPatientView.visibility = VISIBLE
    setAppointmentReminderView.visibility = GONE
    removeAppointmentView.visibility = GONE
  }

  override fun switchToSetAppointmentReminderView() {
    callPatientView.visibility = GONE
    setAppointmentReminderView.visibility = VISIBLE
    removeAppointmentView.visibility = GONE
  }

  override fun switchToRemoveAppointmentView() {
    callPatientView.visibility = GONE
    setAppointmentReminderView.visibility = GONE
    removeAppointmentView.visibility = VISIBLE
  }

  override fun renderAppointmentRemoveReasons(reasons: List<RemoveAppointmentReason>, selectedReason: RemoveAppointmentReason?) {
    removeAppointmentView.renderAppointmentRemoveReasons(reasons, selectedReason)
  }

  override fun enableRemoveAppointmentDoneButton() {
    removeAppointmentView.enableRemoveAppointmentDoneButton()
  }

  override fun disableRemoveAppointmentDoneButton() {
    removeAppointmentView.disableRemoveAppointmentDoneButton()
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

  private fun removeAppointmentCloseClicks(): Observable<ContactPatientEvent> {
    return Observable.create { emitter ->
      emitter.setCancellable { removeAppointmentView.closeClicked = null }

      removeAppointmentView.closeClicked = { emitter.onNext(BackClicked) }
    }
  }

  private fun removeAppointmentReasonSelections(): Observable<ContactPatientEvent> {
    return Observable.create { emitter ->
      emitter.setCancellable { removeAppointmentView.removeReasonClicked = null }

      removeAppointmentView.removeReasonClicked = { emitter.onNext(RemoveAppointmentReasonSelected(it)) }
    }
  }

  private fun removeAppointmentDoneClicks(): Observable<ContactPatientEvent> {
    return Observable.create { emitter ->
      emitter.setCancellable { removeAppointmentView.doneClicked = null }

      removeAppointmentView.doneClicked = { emitter.onNext(RemoveAppointmentDoneClicked) }
    }
  }

  @Parcelize
  data class Key(val patientId: UUID) : ScreenKey() {

    override val analyticsName = "Contact Patient Bottom Sheet"

    override val type = ScreenType.Modal

    override fun instantiateFragment() = ContactPatientBottomSheet()
  }

  interface Injector {
    fun inject(target: ContactPatientBottomSheet)
  }
}
