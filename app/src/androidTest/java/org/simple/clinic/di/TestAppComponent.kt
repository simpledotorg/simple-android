package org.simple.clinic.di

import dagger.Component
import org.simple.clinic.TestRedApp
import org.simple.clinic.bp.sync.BloodPressureSyncAndroidTest
import org.simple.clinic.patient.PatientRepositoryAndroidTest
import org.simple.clinic.patient.PatientSyncAndroidTest

@AppScope
@Component(modules = [AppModule::class])
interface TestAppComponent : AppComponent {

  fun inject(target: PatientSyncAndroidTest)
  fun inject(target: BloodPressureSyncAndroidTest)
  fun inject(target: PatientRepositoryAndroidTest)
  fun inject(target: TestRedApp)
}
