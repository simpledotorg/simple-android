package org.simple.clinic.scheduleappointment.facilityselection

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity

@Subcomponent
interface FacilitySelectionActivityComponent {

  fun inject(activity: FacilitySelectionActivity)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): FacilitySelectionActivityComponent
  }
}
