package org.simple.clinic.drugs.selection.entry.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.drugs.selection.entry.CustomPrescriptionEntrySheet
import org.simple.clinic.drugs.selection.entry.confirmremovedialog.ConfirmRemovePrescriptionDialog

@Subcomponent(modules = [AssistedInjectModule::class])
interface CustomPrescriptionEntrySheetComponent: ConfirmRemovePrescriptionDialog.Injector {

  fun inject(target: CustomPrescriptionEntrySheet)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): CustomPrescriptionEntrySheetComponent
  }
}
