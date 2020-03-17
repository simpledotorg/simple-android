package org.simple.clinic.drugs.selection.entry.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.drugs.selection.entry.CustomPrescriptionEntrySheet

@Subcomponent(modules = [AssistedInjectModule::class])
interface CustomPrescriptionEntrySheetComponent {

  fun inject(target: CustomPrescriptionEntrySheet)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): CustomPrescriptionEntrySheetComponent
  }
}
