package org.simple.clinic.bloodsugar.entry.di

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet
import org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar.ConfirmRemoveBloodSugarDialogInjector
import org.simple.clinic.bloodsugar.unitselection.BloodSugarUnitSelectionDialog.BloodSugarUnitSelectionDialogInjector

@Subcomponent
interface BloodSugarEntryComponent : ConfirmRemoveBloodSugarDialogInjector, BloodSugarUnitSelectionDialogInjector {

  fun inject(target: BloodSugarEntrySheet)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): BloodSugarEntryComponent
  }
}
