package org.simple.clinic.bp.assignbppassport

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface BpPassportSheetComponent {

  fun inject(target: BpPassportSheet)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {
    fun build(): BpPassportSheetComponent
  }
}
