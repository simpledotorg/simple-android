package org.simple.clinic.di

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.res.Resources
import android.os.Vibrator
import androidx.work.WorkManager
import com.f2prateek.rx.preferences2.Preference
import com.google.android.gms.common.GoogleApiAvailability
import dagger.Module
import dagger.Provides
import org.simple.clinic.BuildConfig
import org.simple.clinic.appconfig.AppConfigModule
import org.simple.clinic.appconfig.AppLockModule
import org.simple.clinic.appconfig.CountryModule
import org.simple.clinic.di.network.HttpInterceptorsModule
import org.simple.clinic.di.network.NetworkModule
import org.simple.clinic.di.network.RetrofitModule
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyModule
import org.simple.clinic.enterotp.BruteForceOtpEntryProtectionModule
import org.simple.clinic.facility.change.FacilityChangeModule
import org.simple.clinic.home.overdue.OverdueAppointmentsConfigModule
import org.simple.clinic.instantsearch.InstantSearchConfigModule
import org.simple.clinic.login.LoginModule
import org.simple.clinic.login.LoginOtpSmsListenerModule
import org.simple.clinic.onboarding.OnboardingModule
import org.simple.clinic.patient.PatientModule
import org.simple.clinic.plumbing.infrastructure.InfrastructureModule
import org.simple.clinic.registration.RegistrationModule
import org.simple.clinic.remoteconfig.RemoteConfigModule
import org.simple.clinic.remoteconfig.firebase.FirebaseRemoteConfigModule
import org.simple.clinic.security.di.PinVerificationModule
import org.simple.clinic.security.pin.BruteForceProtectionModule
import org.simple.clinic.settings.SettingsModule
import org.simple.clinic.simplevideo.SimpleVideoModule
import org.simple.clinic.storage.StorageModule
import org.simple.clinic.summary.PatientSummaryModule
import org.simple.clinic.sync.DataSyncOnApprovalModule
import org.simple.clinic.sync.SyncConfigModule
import org.simple.clinic.sync.SyncModule
import org.simple.clinic.sync.indicator.SyncIndicatorModule
import org.simple.clinic.teleconsultlog.prescription.TeleconsultPrescriptionModule
import org.simple.clinic.user.SessionModule
import org.simple.clinic.user.clearpatientdata.ClearPatientDataModule
import org.simple.clinic.util.ElapsedRealtimeClock
import org.simple.clinic.util.RealUserClock
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.DefaultSchedulersProvider
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toNullable
import java.time.ZoneId
import java.util.Locale
import java.util.Optional
import javax.inject.Named

@Module(includes = [
  SyncModule::class,
  NetworkModule::class,
  StorageModule::class,
  LoginModule::class,
  RegistrationModule::class,
  FacilityChangeModule::class,
  BruteForceProtectionModule::class,
  PatientSummaryModule::class,
  DateFormatterModule::class,
  PatientModule::class,
  SyncIndicatorModule::class,
  OnboardingModule::class,
  DataSyncOnApprovalModule::class,
  SimpleVideoModule::class,
  RemoteConfigModule::class,
  SettingsModule::class,
  AppConfigModule::class,
  FirebaseRemoteConfigModule::class,
  LoginOtpSmsListenerModule::class,
  HttpInterceptorsModule::class,
  RetrofitModule::class,
  ClearPatientDataModule::class,
  FlipperModule::class,
  PinVerificationModule::class,
  SessionModule::class,
  UuidGeneratorModule::class,
  SyncConfigModule::class,
  AppLockModule::class,
  TeleconsultPrescriptionModule::class,
  InstantSearchConfigModule::class,
  CountryModule::class,
  OverdueAppointmentsConfigModule::class,
  BruteForceOtpEntryProtectionModule::class,
  DrugFrequencyModule::class,
  InfrastructureModule::class
])
class AppModule(private val appContext: Application) {

  @Provides
  fun appContext(): Application {
    return appContext
  }

  @Provides
  fun vibrator() = appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

  @Provides
  fun workManager(appContext: Application) = WorkManager.getInstance(appContext)

  @Provides
  @AppScope
  fun utcClock() = UtcClock()

  @Provides
  @AppScope
  fun userClock(userTimeZone: ZoneId): UserClock = RealUserClock(userTimeZone)

  @Provides
  @AppScope
  fun elapsedRealtimeClock() = ElapsedRealtimeClock()

  @Provides
  fun currentLocale(@Named("preference_user_selected_locale") userSelectedLocalePreference: Preference<Optional<Locale>>): Locale {
    val savedLocale = userSelectedLocalePreference.get().toNullable()
    return savedLocale ?: Locale.getDefault()
  }

  @Provides
  @AppScope
  fun systemDefaultZone(): ZoneId = ZoneId.systemDefault()

  @Provides
  fun resources(): Resources = appContext.resources

  @Provides
  @AppScope
  fun schedulersProvider(): SchedulersProvider = DefaultSchedulersProvider()

  @Provides
  @AppScope
  fun providesPackageInfo(appContext: Application): PackageInfo {
    return appContext
        .packageManager
        .getPackageInfo(BuildConfig.APPLICATION_ID, 0)
  }

  @Provides
  fun providesGoogleApiAvailability() = GoogleApiAvailability.getInstance()
}
