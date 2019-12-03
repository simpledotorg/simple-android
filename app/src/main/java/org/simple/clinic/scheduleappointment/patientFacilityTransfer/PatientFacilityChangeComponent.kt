package org.simple.clinic.scheduleappointment.patientFacilityTransfer

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity

@Subcomponent
interface PatientFacilityChangeComponent {

  fun inject(activity: PatientFacilityChangeActivity)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): PatientFacilityChangeComponent
  }
}
