package org.simple.clinic.summary.teleconsultation.contactdoctor

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface ContactDoctorComponent {

  fun inject(target: ContactDoctorSheet)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): ContactDoctorComponent
  }
}
