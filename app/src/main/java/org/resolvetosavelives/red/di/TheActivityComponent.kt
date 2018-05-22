package org.resolvetosavelives.red.di

import dagger.BindsInstance
import dagger.Subcomponent
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.home.bp.NewBpScreen
import org.resolvetosavelives.red.newentry.address.PatientAddressEntryScreen
import org.resolvetosavelives.red.newentry.bp.PatientBpEntryScreen
import org.resolvetosavelives.red.newentry.drugs.PatientCurrentDrugsEntryScreen
import org.resolvetosavelives.red.newentry.phone.PatientPhoneEntryScreen
import org.resolvetosavelives.red.newentry.personal.PatientPersonalDetailsEntryScreen
import org.resolvetosavelives.red.newentry.search.PatientSearchByPhoneScreen
import org.resolvetosavelives.red.newentry.success.PatientSavedScreen
import org.resolvetosavelives.red.qrscan.AadhaarScanScreen
import org.resolvetosavelives.red.router.screen.ScreenRouter

@Subcomponent
interface TheActivityComponent {

  fun inject(target: NewBpScreen)
  fun inject(target: TheActivity)
  fun inject(target: PatientPersonalDetailsEntryScreen)
  fun inject(target: PatientAddressEntryScreen)
  fun inject(target: PatientBpEntryScreen)
  fun inject(target: PatientCurrentDrugsEntryScreen)
  fun inject(target: PatientPhoneEntryScreen)
  fun inject(target: PatientSavedScreen)
  fun inject(target: PatientSearchByPhoneScreen)
  fun inject(target: AadhaarScanScreen)

  @Subcomponent.Builder
  interface Builder {

    @BindsInstance
    fun activity(theActivity: TheActivity): Builder

    @BindsInstance
    fun screenRouter(screenRouter: ScreenRouter): Builder

    fun build(): TheActivityComponent
  }
}
