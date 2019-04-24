package org.simple.clinic.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.WindowManager
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.BuildConfig
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.analytics.Analytics
import org.simple.clinic.home.patients.LoggedOutOnOtherDeviceDialog
import org.simple.clinic.login.applock.AppLockScreenKey
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.screen.FullScreenKeyChanger
import org.simple.clinic.router.screen.NestedKeyChanger
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.screen.FullScreenKeyChangeAnimator
import org.simple.clinic.widgets.TheActivityLifecycle
import javax.inject.Inject

class TheActivity : AppCompatActivity() {

  companion object {
    lateinit var component: TheActivityComponent
  }

  @Inject
  lateinit var controller: TheActivityController

  @Inject
  lateinit var lifecycle: Observable<TheActivityLifecycle>

  lateinit var screenRouter: ScreenRouter

  private val screenResults: ScreenResultBus = ScreenResultBus()

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    @Suppress("ConstantConditionIf")
    if (BuildConfig.DISABLE_SCREENSHOT) {
      window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    lifecycle
        .startWith(TheActivityLifecycle.Started())
        .compose(controller)
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(lifecycle.ofType<TheActivityLifecycle.Destroyed>())
        .subscribe { uiChange -> uiChange(this) }
  }

  override fun attachBaseContext(baseContext: Context) {
    val contextWithRouter = wrapContextWithRouter(baseContext)
    super.attachBaseContext(ViewPumpContextWrapper.wrap(contextWithRouter))
  }

  private fun wrapContextWithRouter(baseContext: Context): Context {
    screenRouter = ScreenRouter.create(this, NestedKeyChanger(), screenResults)
    component = ClinicApp.appComponent
        .activityComponentBuilder()
        .activity(this)
        .screenRouter(screenRouter)
        .build()
    component.inject(this)

    screenRouter.registerKeyChanger(FullScreenKeyChanger(
        activity = this,
        screenLayoutContainerRes = android.R.id.content,
        screenBackgroundRes = R.color.window_background,
        onKeyChange = this::onScreenChanged,
        keyChangeAnimator = FullScreenKeyChangeAnimator()
    ))
    return screenRouter.installInContext(baseContext, controller.initialScreenKey())
  }

  private fun onScreenChanged(outgoing: FullScreenKey?, incoming: FullScreenKey) {
    val outgoingScreenName = outgoing?.analyticsName ?: ""
    val incomingScreenName = incoming.analyticsName
    Analytics.reportScreenChange(outgoingScreenName, incomingScreenName)
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

  fun showAppLockScreen() {
    screenRouter.push(AppLockScreenKey())
  }

  // This is here because we need to show the same alert in multiple
  // screens when the user gets verified in the background.
  fun showUserLoggedOutOnOtherDeviceAlert() {
    LoggedOutOnOtherDeviceDialog.show(supportFragmentManager)
  }
}
