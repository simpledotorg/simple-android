package org.simple.clinic.facility.alertchange

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface AlertFacilityChangeComponent {

  fun inject(target: AlertFacilityChangeSheet)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): AlertFacilityChangeComponent
  }
}
