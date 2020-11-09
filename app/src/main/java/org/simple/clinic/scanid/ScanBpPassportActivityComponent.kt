package org.simple.clinic.scanid

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule

@Subcomponent(modules = [AssistedInjectModule::class])
interface ScanBpPassportActivityComponent: ScanSimpleIdScreen.Injector {

  fun inject(target: ScanBpPassportActivity)

  @Subcomponent.Builder
  interface Builder : BindsActivity<Builder> {
    fun build(): ScanBpPassportActivityComponent
  }
}
