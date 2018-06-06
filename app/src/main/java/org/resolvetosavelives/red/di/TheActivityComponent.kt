package org.resolvetosavelives.red.di

import dagger.BindsInstance
import dagger.Subcomponent
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.home.bp.NewBpScreen
import org.resolvetosavelives.red.newentry.PatientEntryScreen
import org.resolvetosavelives.red.newentry.success.PatientSavedScreen
import org.resolvetosavelives.red.qrscan.AadhaarScanScreen
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.search.PatientSearchScreen

@Subcomponent
interface TheActivityComponent {

  fun inject(target: NewBpScreen)
  fun inject(target: TheActivity)
  fun inject(target: PatientSavedScreen)
  fun inject(target: PatientSearchScreen)
  fun inject(target: AadhaarScanScreen)
  fun inject(target: PatientEntryScreen)

  @Subcomponent.Builder
  interface Builder {

    @BindsInstance
    fun activity(theActivity: TheActivity): Builder

    @BindsInstance
    fun screenRouter(screenRouter: ScreenRouter): Builder

    fun build(): TheActivityComponent
  }
}
