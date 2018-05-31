package org.resolvetosavelives.red.di

import dagger.Component
import org.resolvetosavelives.red.sync.PatientSyncAndroidTest

@AppScope
@Component(modules = [AppModule::class])
interface TestAppComponent : AppComponent {

  fun inject(target: PatientSyncAndroidTest)
}
