package org.simple.clinic.activity

import com.f2prateek.rx.preferences2.Preference
import com.f2prateek.rx.preferences2.RxSharedPreferences
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import org.simple.clinic.bp.entry.BloodPressureEntrySheet
import org.simple.clinic.drugs.selection.ConfirmDeletePrescriptionDialog
import org.simple.clinic.drugs.selection.PrescribedDrugsScreen
import org.simple.clinic.drugs.selection.entry.CustomPrescriptionEntrySheet
import org.simple.clinic.home.bp.NewBpScreen
import org.simple.clinic.login.applock.AppLockScreen
import org.simple.clinic.login.phone.LoginPhoneScreen
import org.simple.clinic.login.pin.LoginPinScreen
import org.simple.clinic.newentry.PatientEntryScreen
import org.simple.clinic.onboarding.OnboardingModule
import org.simple.clinic.qrscan.AadhaarScanScreen
import org.simple.clinic.registration.name.RegistrationFullNameScreen
import org.simple.clinic.registration.phone.RegistrationPhoneScreen
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.PatientSearchAgeFilterSheet
import org.simple.clinic.search.PatientSearchScreen
import org.simple.clinic.summary.PatientSummaryScreen
import org.simple.clinic.util.InstantRxPreferencesConverter
import org.simple.clinic.widgets.RxTheActivityLifecycle
import org.threeten.bp.Instant
import javax.inject.Named

@Subcomponent(modules = [TheActivityModule::class])
interface TheActivityComponent {

  fun inject(target: TheActivity)
  fun inject(target: NewBpScreen)
  fun inject(target: LoginPhoneScreen)
  fun inject(target: LoginPinScreen)
  fun inject(target: AppLockScreen)
  fun inject(target: AadhaarScanScreen)
  fun inject(target: PatientEntryScreen)
  fun inject(target: PatientSearchScreen)
  fun inject(target: PatientSummaryScreen)
  fun inject(target: PrescribedDrugsScreen)
  fun inject(target: BloodPressureEntrySheet)
  fun inject(target: PatientSearchAgeFilterSheet)
  fun inject(target: CustomPrescriptionEntrySheet)
  fun inject(target: ConfirmDeletePrescriptionDialog)
  fun inject(target: RegistrationPhoneScreen)
  fun inject(target: RegistrationFullNameScreen)

  @Subcomponent.Builder
  interface Builder {

    @BindsInstance
    fun activity(theActivity: TheActivity): Builder

    @BindsInstance
    fun screenRouter(screenRouter: ScreenRouter): Builder

    fun build(): TheActivityComponent
  }
}

@Module(includes = [OnboardingModule::class])
class TheActivityModule {

  @Provides
  fun theActivityLifecycle(activity: TheActivity): RxTheActivityLifecycle {
    return RxTheActivityLifecycle.from(activity)
  }

  @Provides
  @Named("should_lock_after")
  fun lastAppStopTimestamp(rxSharedPrefs: RxSharedPreferences): Preference<Instant> {
    return rxSharedPrefs.getObject("should_lock_after", Instant.MAX, InstantRxPreferencesConverter())
  }
}
