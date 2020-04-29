package org.simple.clinic.facility.alertchange

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface AlertFacilityChangeComponent {

  fun inject(target: AlertFacilityChangeSheet)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): AlertFacilityChangeComponent
  }
}
