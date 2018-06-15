package org.simple.clinic

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import org.simple.clinic.di.TheActivityComponent
import org.simple.clinic.home.HomeScreen
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.screen.NestedKeyChanger
import org.simple.clinic.router.screen.ScreenRouter

class TheActivity : AppCompatActivity() {

  companion object {
    lateinit var component: TheActivityComponent
  }

  lateinit var screenRouter: ScreenRouter
  private val screenResults: ScreenResultBus = ScreenResultBus()

  override fun attachBaseContext(baseContext: Context) {
    screenRouter = ScreenRouter.create(this, NestedKeyChanger(), screenResults)
    component = Clinic.appComponent
        .activityComponentBuilder()
        .activity(this)
        .screenRouter(screenRouter)
        .build()
    component.inject(this)

    val contextWithRouter = screenRouter.installInContext(baseContext, android.R.id.content, initialScreenKey())
    super.attachBaseContext(contextWithRouter)
  }

  private fun initialScreenKey(): FullScreenKey {
    return HomeScreen.KEY
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    screenResults.send(ActivityResult(requestCode, resultCode, data))
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    screenResults.send(ActivityPermissionResult(requestCode))
  }

  override fun onBackPressed() {
    val interceptCallback = screenRouter.offerBackPressToInterceptors()
    if (interceptCallback.intercepted) {
      return
    }
    val popCallback = screenRouter.pop()
    if (popCallback.popped) {
      return
    }
    super.onBackPressed()
  }
}
