package org.simple.clinic.screen

import dagger.Module
import dagger.Provides
import org.simple.clinic.remoteconfig.ConfigReader
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.screen.KeyChangeAnimator

@Module
class KeyChangeAnimatorModule {

  @Provides
  fun provideKeyChangeAnimator(configReader: ConfigReader): KeyChangeAnimator<FullScreenKey> {
    val screenChangeAnimationsEnabled = configReader.boolean("screen_change_animations_enabled", true)

    return if (screenChangeAnimationsEnabled) FullScreenKeyChangeAnimator() else FullScreenKeyChangeNoOpAnimator()
  }
}
