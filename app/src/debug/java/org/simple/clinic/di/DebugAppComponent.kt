package org.simple.clinic.di

import dagger.Component
import org.simple.clinic.DebugClinicApp

@AppScope
@Component(modules = [AppModule::class])
interface DebugAppComponent : AppComponent {

  fun inject(target: DebugClinicApp)

}
