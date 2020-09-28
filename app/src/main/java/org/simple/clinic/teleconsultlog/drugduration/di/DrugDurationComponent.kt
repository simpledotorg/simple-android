package org.simple.clinic.teleconsultlog.drugduration.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.teleconsultlog.drugduration.DrugDurationSheet

@Subcomponent(modules = [AssistedInjectModule::class, DrugDurationModule::class])
interface DrugDurationComponent {

  fun inject(target: DrugDurationSheet)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): DrugDurationComponent
  }
}

