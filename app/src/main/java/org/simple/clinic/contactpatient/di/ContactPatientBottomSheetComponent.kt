package org.simple.clinic.contactpatient.di

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.contactpatient.ContactPatientBottomSheet
import org.simple.clinic.contactpatient.views.SetAppointmentReminderView
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface ContactPatientBottomSheetComponent : SetAppointmentReminderView.Injector {

  fun inject(target: ContactPatientBottomSheet)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): ContactPatientBottomSheetComponent
  }
}
