package org.simple.clinic.facility.change.confirm

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.facility.change.FacilityChangeActivity
import org.simple.clinic.facilitypicker.FacilityPickerView

@Subcomponent(
    modules = [AssistedInjectModule::class]
)
interface FacilityChangeComponent : FacilityPickerView.Injector {

  fun inject(activity: FacilityChangeActivity)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): FacilityChangeComponent
  }
}
