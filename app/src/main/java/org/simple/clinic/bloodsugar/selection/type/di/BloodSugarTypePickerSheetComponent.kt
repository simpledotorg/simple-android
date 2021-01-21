package org.simple.clinic.bloodsugar.selection.type.di

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.bloodsugar.selection.type.BloodSugarTypePickerSheet

@Subcomponent
interface BloodSugarTypePickerSheetComponent {

  fun inject(target: BloodSugarTypePickerSheet)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): BloodSugarTypePickerSheetComponent
  }
}
