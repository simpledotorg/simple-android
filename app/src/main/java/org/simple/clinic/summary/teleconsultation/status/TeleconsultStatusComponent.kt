package org.simple.clinic.summary.teleconsultation.status

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface TeleconsultStatusComponent {

  fun inject(target: TeleconsultStatusSheet)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): TeleconsultStatusComponent
  }
}
