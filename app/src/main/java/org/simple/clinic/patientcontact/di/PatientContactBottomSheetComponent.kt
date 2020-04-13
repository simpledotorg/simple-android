package org.simple.clinic.patientcontact.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.patientcontact.PatientContactBottomSheet

@Subcomponent(modules = [AssistedInjectModule::class])
interface PatientContactBottomSheetComponent {

  fun inject(target: PatientContactBottomSheet)

  @Subcomponent.Builder
  interface Builder: BindsActivity<Builder> {

    fun build(): PatientContactBottomSheetComponent
  }
}
