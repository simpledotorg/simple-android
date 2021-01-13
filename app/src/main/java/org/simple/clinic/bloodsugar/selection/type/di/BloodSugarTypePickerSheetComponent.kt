package org.simple.clinic.bloodsugar.selection.type.di

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.bloodsugar.selection.type.BloodSugarTypePickerSheet
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface BloodSugarTypePickerSheetComponent {

  fun inject(target: BloodSugarTypePickerSheet)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): BloodSugarTypePickerSheetComponent
  }
}
