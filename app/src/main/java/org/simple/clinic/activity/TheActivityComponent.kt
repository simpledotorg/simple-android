package org.simple.clinic.activity

import android.support.v4.app.FragmentManager
import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import io.reactivex.Observable
import org.simple.clinic.bp.entry.BloodPressureEntrySheet
import org.simple.clinic.drugs.selection.ConfirmDeletePrescriptionDialog
import org.simple.clinic.drugs.selection.PrescribedDrugsScreen
import org.simple.clinic.drugs.selection.entry.CustomPrescriptionEntrySheet
import org.simple.clinic.enterotp.EnterOtpScreen
import org.simple.clinic.facility.change.FacilityChangeScreen
import org.simple.clinic.forgotpin.confirmpin.ForgotPinConfirmPinScreen
import org.simple.clinic.forgotpin.createnewpin.ForgotPinCreateNewPinScreen
import org.simple.clinic.home.overdue.OverdueScreen
import org.simple.clinic.home.overdue.appointmentreminder.AppointmentReminderSheet
import org.simple.clinic.home.overdue.removepatient.RemoveAppointmentSheet
import org.simple.clinic.home.patients.PatientsModule
import org.simple.clinic.home.patients.PatientsScreen
import org.simple.clinic.login.applock.AppLockScreen
import org.simple.clinic.login.applock.ConfirmResetPinDialog
import org.simple.clinic.login.pin.LoginPinScreen
import org.simple.clinic.medicalhistory.newentry.NewMedicalHistoryScreen
import org.simple.clinic.newentry.PatientEntryScreen
import org.simple.clinic.onboarding.OnboardingModule
import org.simple.clinic.onboarding.OnboardingScreen
import org.simple.clinic.phone.PhoneModule
import org.simple.clinic.qrscan.AadhaarScanScreen
import org.simple.clinic.registration.confirmpin.RegistrationConfirmPinScreen
import org.simple.clinic.registration.facility.RegistrationFacilitySelectionScreen
import org.simple.clinic.registration.location.RegistrationLocationPermissionScreen
import org.simple.clinic.registration.name.RegistrationFullNameScreen
import org.simple.clinic.registration.phone.RegistrationPhoneScreen
import org.simple.clinic.registration.pin.RegistrationPinScreen
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.scheduleappointment.ScheduleAppointmentSheet
import org.simple.clinic.search.PatientSearchModule
import org.simple.clinic.search.PatientSearchScreen
import org.simple.clinic.search.results.PatientSearchResultsScreen
import org.simple.clinic.summary.PatientSummaryModule
import org.simple.clinic.summary.PatientSummaryScreen
import org.simple.clinic.util.InstantRxPreferencesConverter
import org.simple.clinic.widgets.RxTheActivityLifecycle
import org.simple.clinic.widgets.TheActivityLifecycle
import org.threeten.bp.Instant
import javax.inject.Named

@Subcomponent(modules = [TheActivityModule::class])
interface TheActivityComponent {

  fun inject(target: TheActivity)
  fun inject(target: PatientsScreen)
  fun inject(target: LoginPinScreen)
  fun inject(target: AppLockScreen)
  fun inject(target: OverdueScreen)
  fun inject(target: OnboardingScreen)
  fun inject(target: AadhaarScanScreen)
  fun inject(target: PatientEntryScreen)
  fun inject(target: PatientSearchScreen)
  fun inject(target: PatientSearchResultsScreen)
  fun inject(target: PatientSummaryScreen)
  fun inject(target: PrescribedDrugsScreen)
  fun inject(target: BloodPressureEntrySheet)
  fun inject(target: CustomPrescriptionEntrySheet)
  fun inject(target: ConfirmDeletePrescriptionDialog)
  fun inject(target: RegistrationPhoneScreen)
  fun inject(target: RegistrationFullNameScreen)
  fun inject(target: RegistrationPinScreen)
  fun inject(target: RegistrationConfirmPinScreen)
  fun inject(target: RegistrationLocationPermissionScreen)
  fun inject(target: RegistrationFacilitySelectionScreen)
  fun inject(target: FacilityChangeScreen)
  fun inject(target: EnterOtpScreen)
  fun inject(target: ScheduleAppointmentSheet)
  fun inject(target: ConfirmResetPinDialog)
  fun inject(target: ForgotPinCreateNewPinScreen)
  fun inject(target: ForgotPinConfirmPinScreen)
  fun inject(target: AppointmentReminderSheet)
  fun inject(target: RemoveAppointmentSheet)
  fun inject(target: NewMedicalHistoryScreen)

  @Subcomponent.Builder
  interface Builder {

    @BindsInstance
    fun activity(theActivity: TheActivity): Builder

    @BindsInstance
    fun screenRouter(screenRouter: ScreenRouter): Builder

    fun build(): TheActivityComponent
  }
}

@Module(includes = [
  OnboardingModule::class,
  PatientsModule::class,
  PatientSearchModule::class,
  PatientSummaryModule::class,
  PhoneModule::class])
class TheActivityModule {

  @Provides
  fun theActivityLifecycle(activity: TheActivity): Observable<TheActivityLifecycle> {
    return RxTheActivityLifecycle.from(activity).stream()
  }

  @Provides
  @Named("should_lock_after")
  fun lastAppStopTimestamp(rxSharedPrefs: RxSharedPreferences): Preference<Instant> {
    return rxSharedPrefs.getObject("should_lock_after", Instant.MAX, InstantRxPreferencesConverter())
  }

  @Provides
  fun fragmentManager(activity: TheActivity): FragmentManager = activity.supportFragmentManager
}
