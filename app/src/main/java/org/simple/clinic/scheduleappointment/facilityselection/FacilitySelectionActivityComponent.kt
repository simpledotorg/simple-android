package org.simple.clinic.scheduleappointment.facilityselection

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.facilitypicker.FacilityPickerView

@Subcomponent(
    modules = [
      AssistedInjectModule::class
    ]
)
interface FacilitySelectionActivityComponent : FacilityPickerView.Injector {

  fun inject(activity: FacilitySelectionActivity)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): FacilitySelectionActivityComponent
  }
}
