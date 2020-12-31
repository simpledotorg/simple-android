package org.simple.clinic.activity

import dagger.BindsInstance
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ScreenRouter

interface BindsScreenResultBus<B> {

  @BindsInstance
  fun screenResultBus(resultBus: ScreenResultBus): B
}
