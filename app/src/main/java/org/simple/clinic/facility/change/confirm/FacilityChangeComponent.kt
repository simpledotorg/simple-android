package org.simple.clinic.facility.change.confirm

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.facility.change.FacilityChangeScreen

@Subcomponent
interface FacilityChangeComponent {

  fun inject(activity: FacilityChangeScreen)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): FacilityChangeComponent
  }
}
