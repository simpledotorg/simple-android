package org.simple.clinic.facility.alertchange

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface AlertFacilityChangeComponent {

  fun inject(target: AlertFacilityChangeSheet)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): AlertFacilityChangeComponent
  }
}
