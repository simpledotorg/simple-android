package org.simple.clinic.contactpatient

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.sheet_contact_patient.*
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.Gender
import org.simple.clinic.contactpatient.di.ContactPatientBottomSheetComponent
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.phone.Dialer
import org.simple.clinic.phone.PhoneCaller
import org.simple.clinic.phone.PhoneNumberMaskerConfig
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.RequestPermissions
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.wrap
import org.simple.clinic.widgets.BottomSheetActivity
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class ContactPatientBottomSheet : BottomSheetActivity(), ContactPatientUi, ContactPatientUiActions {

  companion object {
    private const val KEY_PATIENT_UUID = "patient_uuid"

    fun intent(context: Context, patientUuid: UUID): Intent {
      return Intent(context, ContactPatientBottomSheet::class.java).apply {
        putExtra(KEY_PATIENT_UUID, patientUuid)
      }
    }
  }

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

  private lateinit var component: ContactPatientBottomSheetComponent

  private val patientUuid by unsafeLazy { intent.getSerializableExtra(KEY_PATIENT_UUID) as UUID }

  private val uiRenderer by unsafeLazy { ContactPatientUiRenderer(this, userClock) }

  private val permissionResults: Subject<ActivityPermissionResult> = PublishSubject.create()

  private val events: Observable<ContactPatientEvent> by unsafeLazy {
    Observable
        .merge(
            normalCallClicks(),
            secureCallClicks(),
            agreedToVisitClicks()
        )
        .compose(RequestPermissions<ContactPatientEvent>(runtimePermissions, this, permissionResults))
        .compose(ReportAnalyticsEvents())
        .cast<ContactPatientEvent>()

  }

  private val delegate by unsafeLazy {
    MobiusDelegate.forActivity(
        events = events,
        defaultModel = ContactPatientModel.create(patientUuid, phoneMaskConfig, appointmentConfig, userClock),
        update = ContactPatientUpdate(phoneMaskConfig),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = ContactPatientInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.sheet_contact_patient)

    delegate.onRestoreInstanceState(savedInstanceState)
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
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun onBackgroundClick() {
    finish()
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
        .patientContactBottomSheetComponent()
        .activity(this)
        .build()

    component.inject(this)
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
    finish()
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
}
