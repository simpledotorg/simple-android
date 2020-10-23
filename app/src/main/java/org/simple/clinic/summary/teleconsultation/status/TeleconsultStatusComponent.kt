package org.simple.clinic.summary.teleconsultation.status

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface TeleconsultStatusComponent {

  fun inject(target: TeleconsultStatusSheet)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): TeleconsultStatusComponent
  }
}
