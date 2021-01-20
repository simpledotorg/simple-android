package org.simple.clinic.bp.assignbppassport

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
interface BpPassportSheetComponent {

  fun inject(target: BpPassportSheet)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): BpPassportSheetComponent
  }
}
