package org.simple.clinic.scheduleappointment.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.scheduleappointment.ScheduleAppointmentSheet

@Subcomponent(modules = [AssistedInjectModule::class])
interface ScheduleAppointmentSheetComponent {

  fun inject(target: ScheduleAppointmentSheet): ScheduleAppointmentSheet

  @Subcomponent.Builder
  interface Builder: BindsActivity<Builder> {
    fun build(): ScheduleAppointmentSheetComponent
  }
}
