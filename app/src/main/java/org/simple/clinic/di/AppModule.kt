package org.simple.clinic.di

import android.app.Application
import android.content.Context
import android.os.Vibrator
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import org.simple.clinic.crash.CrashReporterModule
import org.simple.clinic.login.LoginModule
import org.simple.clinic.patient.PatientModule
import org.simple.clinic.qrscan.QrModule
import org.simple.clinic.registration.RegistrationModule
import org.simple.clinic.security.pin.BruteForceProtectionModule
import org.simple.clinic.storage.StorageModule
import org.simple.clinic.summary.PatientSummaryModule
import org.simple.clinic.sync.SyncModule
import org.simple.clinic.sync.indicator.SyncIndicatorModule
import org.threeten.bp.Clock
import org.threeten.bp.ZoneId
import java.util.Locale

@Module(includes = [
  QrModule::class,
  SyncModule::class,
  NetworkModule::class,
  StorageModule::class,
  LoginModule::class,
  RegistrationModule::class,
  CrashReporterModule::class,
  BruteForceProtectionModule::class,
  PatientSummaryModule::class,
  DateFormatterModule::class,
  PatientModule::class,
  SyncIndicatorModule::class
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
  open fun clock(): Clock = Clock.systemUTC()

  @Provides
  @AppScope
  fun currentLocale(): Locale = Locale.ENGLISH

  @Provides
  @AppScope
  fun systemDefaultZone(): ZoneId = ZoneId.systemDefault()
}
