package org.simple.clinic.activity

import dagger.BindsInstance
import org.simple.clinic.router.screen.ScreenRouter

interface BindsScreenRouter<B> {

  @BindsInstance
  fun screenRouter(screenRouter: ScreenRouter): B
}
