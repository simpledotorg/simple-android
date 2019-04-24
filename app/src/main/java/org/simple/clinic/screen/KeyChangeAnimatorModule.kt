package org.simple.clinic.screen

import dagger.Binds
import dagger.Module
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.screen.KeyChangeAnimator

@Module
abstract class KeyChangeAnimatorModule {

  @Binds
  abstract fun bindKeyChangeAnimator(fullScreenKeyChangeAnimator: FullScreenKeyChangeNoOpAnimator): KeyChangeAnimator<FullScreenKey>
}
