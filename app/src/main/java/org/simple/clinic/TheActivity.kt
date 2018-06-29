package org.simple.clinic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.f2prateek.rx.preferences2.Preference
import org.simple.clinic.di.TheActivityComponent
import org.simple.clinic.home.HomeScreen
import org.simple.clinic.login.phone.LoginPhoneScreen
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.screen.NestedKeyChanger
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.user.LoggedInUser
import org.simple.clinic.util.Optional
import javax.inject.Inject

class TheActivity : AppCompatActivity() {

  companion object {
    lateinit var component: TheActivityComponent
    const val KEY_INITIAL_SCREEN = "initialScreen"

    fun intentWithInitialScreen(context: Context, initialScreenKey: FullScreenKey): Intent {
      return Intent(context, TheActivity::class.java)
          .putExtra(KEY_INITIAL_SCREEN, initialScreenKey)
    }
  }

  @Inject
  lateinit var loggedInUserPref: Preference<Optional<LoggedInUser>>

  lateinit var screenRouter: ScreenRouter

  private val screenResults: ScreenResultBus = ScreenResultBus()

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)

    // getIntent() returns null from attachBaseContext(), so Flow cannot
    // be initialized with the screen key requested by the caller.
    // It can only be overriden here.
    openInitialScreenSetByCaller()
  }

  override fun onNewIntent(newIntent: Intent) {
    super.onNewIntent(newIntent)
    intent = newIntent
    openInitialScreenSetByCaller()
  }

  private fun openInitialScreenSetByCaller() {
    if (intent.hasExtra(KEY_INITIAL_SCREEN)) {
      screenRouter.clearHistoryAndPush(intent.getParcelableExtra(KEY_INITIAL_SCREEN), RouterDirection.REPLACE)
    }
  }

  override fun attachBaseContext(baseContext: Context) {
    screenRouter = ScreenRouter.create(this, NestedKeyChanger(), screenResults)
    component = ClinicApp.appComponent
        .activityComponentBuilder()
        .activity(this)
        .screenRouter(screenRouter)
        .build()
    component.inject(this)

    val contextWithRouter = screenRouter.installInContext(baseContext, android.R.id.content, initialScreenKey())
    super.attachBaseContext(contextWithRouter)
  }

  private fun initialScreenKey(): FullScreenKey {
    return if (loggedInUserPref.isSet) {
      HomeScreen.KEY
    } else {
      LoginPhoneScreen.KEY("")
    }
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
