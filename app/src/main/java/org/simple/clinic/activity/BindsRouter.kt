package org.simple.clinic.activity

import dagger.BindsInstance
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.router.screen.ScreenRouter

interface BindsRouter<B> {

  @BindsInstance
  fun router(router: Router): B
}
