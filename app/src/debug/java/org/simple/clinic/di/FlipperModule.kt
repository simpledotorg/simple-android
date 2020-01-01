package org.simple.clinic.di

import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import dagger.Module
import dagger.Provides

@Module
class FlipperModule {

  @Provides
  @AppScope
  fun provideFlipperNetworkPlugin(): NetworkFlipperPlugin {
    return NetworkFlipperPlugin()
  }
}
