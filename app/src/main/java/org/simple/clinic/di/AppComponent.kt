package org.simple.clinic.di

import dagger.Component
import org.simple.clinic.ClinicApp
import org.simple.clinic.ReleaseClinicApp
import org.simple.clinic.activity.TheActivityComponent
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.login.OtpSmsReceiver
import org.simple.clinic.newentry.clearbutton.ClearFieldImageButton
import org.simple.clinic.registration.RegistrationWorker
import org.simple.clinic.sync.SyncWorker
import org.simple.clinic.user.UserSession
import javax.inject.Scope

@AppScope
@Component(modules = [(AppModule::class)])
interface AppComponent {

  fun inject(target: ClinicApp)
  fun inject(target: ReleaseClinicApp)
  fun inject(target: SyncWorker)
  fun inject(target: ClearFieldImageButton)
  fun inject(target: RegistrationWorker)
  fun inject(target: OtpSmsReceiver)

  fun activityComponentBuilder(): TheActivityComponent.Builder
  fun userSession(): UserSession
  fun facilitySync(): FacilitySync
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope
