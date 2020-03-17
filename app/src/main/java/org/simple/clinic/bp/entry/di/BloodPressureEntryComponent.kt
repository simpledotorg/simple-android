package org.simple.clinic.bp.entry.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.bp.entry.BloodPressureEntrySheet
import org.simple.clinic.bp.entry.confirmremovebloodpressure.ConfirmRemoveBloodPressureDialog
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface BloodPressureEntryComponent : ConfirmRemoveBloodPressureDialog.Injector {

  fun inject(target: BloodPressureEntrySheet)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {
    fun build(): BloodPressureEntryComponent
  }
}
