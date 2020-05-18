package org.simple.clinic.deeplink.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.deeplink.DeepLinkActivity
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface DeepLinkComponent {

  fun inject(target: DeepLinkActivity)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): DeepLinkComponent
  }
}
