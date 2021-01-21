package org.simple.clinic.drugs.selection.entry.di

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.drugs.selection.entry.CustomPrescriptionEntrySheet
import org.simple.clinic.drugs.selection.entry.confirmremovedialog.ConfirmRemovePrescriptionDialog

@Subcomponent
interface CustomPrescriptionEntrySheetComponent : ConfirmRemovePrescriptionDialog.Injector {

  fun inject(target: CustomPrescriptionEntrySheet)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): CustomPrescriptionEntrySheetComponent
  }
}
