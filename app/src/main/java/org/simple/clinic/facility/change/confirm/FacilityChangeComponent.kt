package org.simple.clinic.facility.change.confirm

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.facility.change.FacilityChangeActivity
import org.simple.clinic.facilitypicker.FacilityPickerView

@Subcomponent(
    modules = [AssistedInjectModule::class]
)
interface FacilityChangeComponent: FacilityPickerView.Injector {

  fun inject(activity: FacilityChangeActivity)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): FacilityChangeComponent
  }
}
