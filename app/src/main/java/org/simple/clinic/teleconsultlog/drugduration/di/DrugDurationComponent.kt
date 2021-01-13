package org.simple.clinic.teleconsultlog.drugduration.di

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.teleconsultlog.drugduration.DrugDurationSheet

@Subcomponent(modules = [AssistedInjectModule::class, DrugDurationModule::class])
interface DrugDurationComponent {

  fun inject(target: DrugDurationSheet)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): DrugDurationComponent
  }
}

