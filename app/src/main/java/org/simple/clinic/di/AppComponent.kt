package org.simple.clinic.di

import dagger.Component
import org.simple.clinic.DebugClinicApp
import org.simple.clinic.ReleaseClinicApp
import org.simple.clinic.newentry.clearbutton.ClearFieldImageButton
import org.simple.clinic.sync.SyncWorker
import javax.inject.Scope

@AppScope
@Component(modules = [(AppModule::class)])
interface AppComponent {

  fun inject(target: DebugClinicApp)
  fun inject(target: ReleaseClinicApp)
  fun inject(target: SyncWorker)
  fun inject(target: ClearFieldImageButton)

  fun activityComponentBuilder(): TheActivityComponent.Builder
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope
