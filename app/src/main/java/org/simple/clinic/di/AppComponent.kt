package org.simple.clinic.di

import dagger.Component
import org.simple.clinic.ClinicApp
import org.simple.clinic.ReleaseClinicApp
import org.simple.clinic.activity.TheActivityComponent
import org.simple.clinic.facility.FacilitySync
import org.simple.clinic.login.OtpSmsReceiver
import org.simple.clinic.newentry.clearbutton.ClearFieldImageButton
import org.simple.clinic.storage.Migration_27_28
import org.simple.clinic.storage.Migration_29_30
import org.simple.clinic.storage.Migration_34_35
import org.simple.clinic.storage.Migration_48_49
import org.simple.clinic.storage.Migration_49_50
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.SyncWorker
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.BottomSheetActivity
import javax.inject.Scope

@AppScope
@Component(modules = [(AppModule::class)])
interface AppComponent {

  fun inject(target: ClinicApp)
  fun inject(target: ReleaseClinicApp)
  fun inject(target: SyncWorker)
  fun inject(target: ClearFieldImageButton)
  fun inject(target: OtpSmsReceiver)
  fun inject(target: DataSync)
  fun inject(target: Migration_27_28)
  fun inject(target: Migration_29_30)
  fun inject(target: Migration_34_35)
  fun inject(target: BottomSheetActivity)
  fun inject(target: Migration_49_50)

  fun activityComponentBuilder(): TheActivityComponent.Builder
  fun userSession(): UserSession
  fun facilitySync(): FacilitySync
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope
