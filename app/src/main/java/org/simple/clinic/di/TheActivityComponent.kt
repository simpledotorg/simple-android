package org.simple.clinic.di

import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import org.simple.clinic.TheActivity
import org.simple.clinic.bp.entry.BloodPressureEntrySheetView
import org.simple.clinic.home.bp.NewBpScreen
import org.simple.clinic.newentry.PatientEntryScreen
import org.simple.clinic.qrscan.AadhaarScanScreen
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.PatientSearchAgeFilterSheet
import org.simple.clinic.search.PatientSearchScreen
import org.simple.clinic.summary.PatientSummaryScreen
import org.simple.clinic.widgets.RxTheActivityLifecycle

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
