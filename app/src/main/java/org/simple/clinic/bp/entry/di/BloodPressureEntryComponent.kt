package org.simple.clinic.bp.entry.di

import androidx.appcompat.app.AppCompatActivity
import dagger.BindsInstance
import dagger.Subcomponent
import org.simple.clinic.bp.entry.BloodPressureEntrySheet
import org.simple.clinic.bp.entry.confirmremovebloodpressure.ConfirmRemoveBloodPressureDialog
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface BloodPressureEntryComponent : ConfirmRemoveBloodPressureDialog.Injector {

  fun inject(target: BloodPressureEntrySheet)

  @Subcomponent.Factory
  interface Factory {
    fun create(@BindsInstance activity: AppCompatActivity): BloodPressureEntryComponent
  }
}
