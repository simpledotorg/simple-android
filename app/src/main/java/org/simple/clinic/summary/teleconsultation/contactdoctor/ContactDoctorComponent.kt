package org.simple.clinic.summary.teleconsultation.contactdoctor

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface ContactDoctorComponent {

  fun inject(target: ContactDoctorSheet)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): ContactDoctorComponent
  }
}
