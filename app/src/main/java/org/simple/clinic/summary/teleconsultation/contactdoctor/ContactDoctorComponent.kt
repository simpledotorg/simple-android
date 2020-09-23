package org.simple.clinic.summary.teleconsultation.contactdoctor

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface ContactDoctorComponent {

  fun inject(target: ContactDoctorSheet)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): ContactDoctorComponent
  }
}
