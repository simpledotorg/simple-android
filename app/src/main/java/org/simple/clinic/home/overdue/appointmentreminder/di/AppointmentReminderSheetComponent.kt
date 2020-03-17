package org.simple.clinic.home.overdue.appointmentreminder.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.home.overdue.appointmentreminder.AppointmentReminderSheet

@Subcomponent(modules = [AssistedInjectModule::class])
interface AppointmentReminderSheetComponent {

  fun inject(target: AppointmentReminderSheet)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {
    fun build(): AppointmentReminderSheetComponent
  }
}
