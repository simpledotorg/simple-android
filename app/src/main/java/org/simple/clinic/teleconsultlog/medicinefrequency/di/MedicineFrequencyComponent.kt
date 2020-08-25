package org.simple.clinic.teleconsultlog.medicinefrequency.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequencySheet

@Subcomponent(modules = [AssistedInjectModule::class])
interface MedicineFrequencyComponent {

  fun inject(target: MedicineFrequencySheet)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {
    fun build(): MedicineFrequencyComponent
  }
}
