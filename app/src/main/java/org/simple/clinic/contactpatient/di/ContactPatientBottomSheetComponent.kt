package org.simple.clinic.contactpatient.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.contactpatient.ContactPatientBottomSheet

@Subcomponent(modules = [AssistedInjectModule::class])
interface ContactPatientBottomSheetComponent {

  fun inject(target: ContactPatientBottomSheet)

  @Subcomponent.Builder
  interface Builder: BindsActivity<Builder> {

    fun build(): ContactPatientBottomSheetComponent
  }
}
