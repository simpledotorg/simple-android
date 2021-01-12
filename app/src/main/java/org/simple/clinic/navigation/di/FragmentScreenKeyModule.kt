package org.simple.clinic.navigation.di

import dagger.Binds
import dagger.Module
import org.simple.clinic.navigation.v2.keyprovider.FlowBasedScreenKeyProvider
import org.simple.clinic.navigation.v2.keyprovider.FragmentBasedScreenKeyProvider
import org.simple.clinic.navigation.v2.keyprovider.ScreenKeyProvider

@Module
abstract class FragmentScreenKeyModule {

  @Binds
  abstract fun provideScreenKeyProvider(
      screenKeyProvider: FragmentBasedScreenKeyProvider
  ): ScreenKeyProvider
}
