package org.resolvetosavelives.red.di

import dagger.Component
import org.resolvetosavelives.red.bp.sync.BloodPressureSyncAndroidTest
import org.resolvetosavelives.red.patient.PatientSyncAndroidTest

@AppScope
@Component(modules = [AppModule::class])
interface TestAppComponent : AppComponent {

  fun inject(target: PatientSyncAndroidTest)
  fun inject(target: BloodPressureSyncAndroidTest)
}
