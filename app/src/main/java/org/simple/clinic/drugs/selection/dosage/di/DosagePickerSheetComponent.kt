package org.simple.clinic.drugs.selection.dosage.di

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.drugs.selection.dosage.DosagePickerSheet

@Subcomponent
interface DosagePickerSheetComponent {

  fun inject(target: DosagePickerSheet)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): DosagePickerSheetComponent
  }
}
