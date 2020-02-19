package org.simple.clinic.bloodsugar.entry.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.bloodsugar.entry.BloodSugarEntrySheet
import org.simple.clinic.bloodsugar.entry.confirmremovebloodsugar.ConfirmRemoveBloodSugarDialogInjector

@Subcomponent(modules = [BloodSugarEntryModule::class])
interface BloodSugarEntryComponent : ConfirmRemoveBloodSugarDialogInjector {

  fun inject(target: BloodSugarEntrySheet)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {

    fun build(): BloodSugarEntryComponent
  }
}
