package org.resolvetosavelives.red.di

import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.bp.entry.BloodPressureEntrySheetView
import org.resolvetosavelives.red.home.bp.NewBpScreen
import org.resolvetosavelives.red.newentry.PatientEntryScreen
import org.resolvetosavelives.red.qrscan.AadhaarScanScreen
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.search.PatientSearchAgeFilterSheet
import org.resolvetosavelives.red.search.PatientSearchScreen
import org.resolvetosavelives.red.summary.PatientSummaryScreen
import org.resolvetosavelives.red.widgets.RxTheActivityLifecycle

@Subcomponent(modules = [TheActivityModule::class])
interface TheActivityComponent {

  fun inject(target: NewBpScreen)
  fun inject(target: TheActivity)
  fun inject(target: PatientSearchScreen)
  fun inject(target: AadhaarScanScreen)
  fun inject(target: PatientEntryScreen)
  fun inject(target: PatientSummaryScreen)
  fun inject(target: BloodPressureEntrySheetView)
  fun inject(target: PatientSearchAgeFilterSheet)

  @Subcomponent.Builder
  interface Builder {

    @BindsInstance
    fun activity(theActivity: TheActivity): Builder

    @BindsInstance
    fun screenRouter(screenRouter: ScreenRouter): Builder

    fun build(): TheActivityComponent
  }
}

@Module
class TheActivityModule {

  @Provides
  fun theActivityLifecycle(activity: TheActivity): RxTheActivityLifecycle {
    return RxTheActivityLifecycle.from(activity)
  }
}
