package org.simple.clinic.setup

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity

@Subcomponent
interface SetupActivityComponent {

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): SetupActivityComponent
  }
}
