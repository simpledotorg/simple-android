package org.simple.clinic.teleconsultlog.medicinefrequency.di

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequencySheet

@Subcomponent(modules = [AssistedInjectModule::class])
interface MedicineFrequencyComponent {

  fun inject(target: MedicineFrequencySheet)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): MedicineFrequencyComponent
  }
}
