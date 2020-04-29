package org.simple.clinic.home.overdue.removepatient.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.home.overdue.removepatient.RemoveAppointmentScreen

@Subcomponent(modules = [AssistedInjectModule::class])
interface RemoveAppointmentScreenComponent {

  fun inject(target: RemoveAppointmentScreen)

  @Subcomponent.Builder
  interface Builder: BindsActivity<Builder> {

    fun build(): RemoveAppointmentScreenComponent
  }
}
