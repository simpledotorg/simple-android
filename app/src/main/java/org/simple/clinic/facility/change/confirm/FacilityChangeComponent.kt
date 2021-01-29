package org.simple.clinic.facility.change.confirm

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.facility.change.FacilityChangeScreen
import org.simple.clinic.facilitypicker.FacilityPickerView

@Subcomponent
interface FacilityChangeComponent : FacilityPickerView.Injector {

  fun inject(activity: FacilityChangeScreen)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): FacilityChangeComponent
  }
}
