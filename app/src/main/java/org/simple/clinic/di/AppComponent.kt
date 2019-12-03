package org.simple.clinic.di

import dagger.Component
import org.simple.clinic.ClinicApp
import org.simple.clinic.login.OtpSmsReceiver
import org.simple.clinic.main.TheActivityComponent
import org.simple.clinic.newentry.clearbutton.ClearFieldImageButton
import org.simple.clinic.scheduleappointment.patientFacilityTransfer.PatientFacilityChangeActivity
import org.simple.clinic.scheduleappointment.patientFacilityTransfer.PatientFacilityChangeComponent
import org.simple.clinic.setup.SetupActivityComponent
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.SyncWorker
import org.simple.clinic.widgets.BottomSheetActivity
import javax.inject.Scope

@AppScope
@Component(modules = [(AppModule::class)])
interface AppComponent {

  fun inject(target: ClinicApp)
  fun inject(target: SyncWorker)
  fun inject(target: ClearFieldImageButton)
  fun inject(target: OtpSmsReceiver)
  fun inject(target: DataSync)
  fun inject(target: BottomSheetActivity)

  fun theActivityComponentBuilder(): TheActivityComponent.Builder
  fun setupActivityComponentBuilder(): SetupActivityComponent.Builder
  fun patientFacilityChangeComponentBuilder() : PatientFacilityChangeComponent.Builder
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope
