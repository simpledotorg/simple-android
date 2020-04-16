package org.simple.clinic.contactpatient.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.contactpatient.views.SetAppointmentReminderView
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface ContactPatientBottomSheetComponent : SetAppointmentReminderView.Injector {

  fun inject(target: ContactPatientBottomSheet)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): ContactPatientBottomSheetComponent
  }
}
