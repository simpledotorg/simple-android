package org.resolvetosavelives.red.di

import dagger.Component
import org.resolvetosavelives.red.ReleaseRedApp
import org.resolvetosavelives.red.sync.SyncWorker
import javax.inject.Scope

@AppScope
@Component(modules = [(AppModule::class)])
interface AppComponent {

  fun inject(target: ReleaseRedApp)
  fun inject(target: SyncWorker)

  fun activityComponentBuilder(): TheActivityComponent.Builder
}

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AppScope
