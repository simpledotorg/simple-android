package org.simple.clinic.home.overdue.phonemask.di

import dagger.Subcomponent
import org.simple.clinic.activity.BindsActivity
import org.simple.clinic.di.AssistedInjectModule
import org.simple.clinic.home.overdue.phonemask.PhoneMaskBottomSheet

@Subcomponent(modules = [AssistedInjectModule::class])
interface PhoneMaskBottomSheetComponent {

  fun inject(target: PhoneMaskBottomSheet)

  @Subcomponent.Builder
  interface Builder: BindsActivity<Builder> {

    fun build(): PhoneMaskBottomSheetComponent
  }
}
