package org.simple.clinic.scheduleappointment.facilityselection

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.facilitypicker.FacilityPickerView

@Subcomponent(
    modules = [
      AssistedInjectModule::class
    ]
)
interface FacilitySelectionActivityComponent : FacilityPickerView.Injector {

  fun inject(activity: FacilitySelectionActivity)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): FacilitySelectionActivityComponent
  }
}
