package org.simple.clinic.deeplink.di

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.deeplink.DeepLinkActivity
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface DeepLinkComponent {

  fun inject(target: DeepLinkActivity)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): DeepLinkComponent
  }
}
