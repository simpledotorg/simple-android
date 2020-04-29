package org.simple.clinic.drugs.selection.dosage.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.drugs.selection.dosage.DosagePickerSheet

@Subcomponent(modules = [AssistedInjectModule::class])
interface DosagePickerSheetComponent {

  fun inject(target: DosagePickerSheet)

  @Subcomponent.Builder
  interface Builder: BindsActivity<Builder> {

    fun build(): DosagePickerSheetComponent
  }
}
