package org.simple.clinic.bloodsugar.selection.type.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.bloodsugar.selection.type.BloodSugarTypePickerSheet
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface BloodSugarTypePickerSheetComponent {

  fun inject(target: BloodSugarTypePickerSheet)

  @Subcomponent.Builder
  interface Builder: BindsActivity<Builder> {

    fun build(): BloodSugarTypePickerSheetComponent
  }
}
