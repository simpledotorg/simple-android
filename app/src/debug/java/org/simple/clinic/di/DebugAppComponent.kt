package org.simple.clinic.di

import dagger.Component
import org.simple.clinic.DebugClinicApp
import org.simple.clinic.DebugNotificationActionReceiver
import org.simple.clinic.FakeDataGenerationReceiver
import org.simple.clinic.WebviewTestActivity

@AppScope
@Component(modules = [AppModule::class])
interface DebugAppComponent : AppComponent {

  fun inject(target: DebugClinicApp)
  fun inject(target: DebugNotificationActionReceiver)
  fun inject(target: FakeDataGenerationReceiver)
  fun inject(target: WebviewTestActivity)
}
