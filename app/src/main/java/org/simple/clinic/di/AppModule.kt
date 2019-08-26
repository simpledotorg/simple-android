package org.simple.clinic.di

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Vibrator
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.appupdate.AppUpdateModule
import org.simple.clinic.crash.CrashReporterModule
import org.simple.clinic.facility.change.FacilityChangeModule
import org.simple.clinic.login.LoginModule
import org.simple.clinic.onboarding.OnboardingModule
import org.simple.clinic.patient.PatientModule
import org.simple.clinic.patient.shortcode.UuidShortCodeCreatorModule
import org.simple.clinic.registration.RegistrationModule
import org.simple.clinic.screen.KeyChangeAnimatorModule
import org.simple.clinic.security.pin.BruteForceProtectionModule
import org.simple.clinic.storage.StorageModule
import org.simple.clinic.summary.PatientSummaryModule
import org.simple.clinic.sync.DataSyncOnApprovalModule
import org.simple.clinic.sync.SyncModule
import org.simple.clinic.sync.indicator.SyncIndicatorModule
import org.simple.clinic.util.scheduler.DefaultSchedulersProvider
import org.simple.clinic.util.ElapsedRealtimeClock
import org.simple.clinic.util.RealUserClock
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.identifierdisplay.IdentifierDisplayAdapterModule
import org.threeten.bp.ZoneId
import java.util.Locale

@Module(includes = [
  SyncModule::class,
  NetworkModule::class,
  StorageModule::class,
  LoginModule::class,
  RegistrationModule::class,
  FacilityChangeModule::class,
  CrashReporterModule::class,
  BruteForceProtectionModule::class,
  PatientSummaryModule::class,
  DateFormatterModule::class,
  PatientModule::class,
  SyncIndicatorModule::class,
  UuidShortCodeCreatorModule::class,
  IdentifierDisplayAdapterModule::class,
  KeyChangeAnimatorModule::class,
  OnboardingModule::class,
  AppUpdateModule::class,
  DataSyncOnApprovalModule::class
])
open class AppModule(private val appContext: Application) {

  @Provides
  fun appContext(): Application {
    return appContext
  }

  @Provides
  fun vibrator() = appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

  @Provides
  fun workManager() = WorkManager.getInstance()

  @Provides
  @AppScope
  open fun utcClock() = UtcClock()

  @Provides
  @AppScope
  open fun userClock(userTimeZone: ZoneId): UserClock = RealUserClock(userTimeZone)

  @Provides
  @AppScope
  open fun elapsedRealtimeClock() = ElapsedRealtimeClock()

  @Provides
  fun currentLocale(): Locale = Locale.getDefault()

  @Provides
  @AppScope
  fun systemDefaultZone(): ZoneId = ZoneId.systemDefault()

  @Provides
  fun resources(): Resources = appContext.resources

  @Provides
  @AppScope
  fun schedulersProvider(): SchedulersProvider = DefaultSchedulersProvider()
}
