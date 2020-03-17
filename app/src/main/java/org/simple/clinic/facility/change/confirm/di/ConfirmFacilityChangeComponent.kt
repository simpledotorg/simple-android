package org.simple.clinic.facility.change.confirm.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.facility.change.confirm.ConfirmFacilityChangeSheet

@Subcomponent(modules = [AssistedInjectModule::class])
interface ConfirmFacilityChangeComponent {

  fun inject(target: ConfirmFacilityChangeSheet)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): ConfirmFacilityChangeComponent
  }
}
