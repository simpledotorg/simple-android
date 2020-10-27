package org.simple.clinic.registerorlogin

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.activity.BindsScreenRouter

@Subcomponent
interface AuthenticationActivityComponent {

  fun inject(target: AuthenticationActivity)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder>, BindsScreenRouter<Builder> {
    fun build(): AuthenticationActivityComponent
  }
}

