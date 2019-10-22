package org.simple.clinic.setup

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity

@Subcomponent
interface SetupActivityComponent {

  fun inject(target: SetupActivity)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): SetupActivityComponent
  }
}
