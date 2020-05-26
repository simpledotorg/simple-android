package org.simple.clinic.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.BuildConfig
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.activity.ActivityLifecycle
import org.simple.clinic.activity.ActivityLifecycle.Destroyed
import org.simple.clinic.activity.ActivityLifecycle.Started
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.deeplink.DeepLinkResult
import org.simple.clinic.deeplink.OpenPatientSummary
import org.simple.clinic.deeplink.ShowNoPatientUuid
import org.simple.clinic.deeplink.ShowPatientNotFound
import org.simple.clinic.deniedaccess.AccessDeniedScreenKey
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.home.patients.LoggedOutOnOtherDeviceDialog
import org.simple.clinic.login.applock.AppLockScreenKey
import org.simple.clinic.registration.phone.RegistrationPhoneScreenKey
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.screen.FullScreenKeyChanger
import org.simple.clinic.router.screen.NestedKeyChanger
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.sync.SyncSetup
import org.simple.clinic.user.UnauthorizeUser
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.wrap
import org.threeten.bp.Instant
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class TheActivity : AppCompatActivity() {

  companion object {
    private const val EXTRA_DEEP_LINK_RESULT = "deep_link_result"

    fun newIntent(context: Context): Intent {
      return Intent(context, TheActivity::class.java)
    }

    fun intentForOpenPatientSummary(context: Context, patientUuid: UUID): Intent {
      return Intent(context, TheActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(EXTRA_DEEP_LINK_RESULT, OpenPatientSummary(patientUuid))
      }
    }

    fun intentForShowPatientNotFoundError(context: Context): Intent {
      return Intent(context, TheActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(EXTRA_DEEP_LINK_RESULT, ShowPatientNotFound)
      }
    }

    fun intentForShowNoPatientUuidError(context: Context): Intent {
      return Intent(context, TheActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(EXTRA_DEEP_LINK_RESULT, ShowNoPatientUuid)
      }
    }

    lateinit var component: TheActivityComponent
  }

  @Inject
  lateinit var controller: TheActivityController

  @Inject
  lateinit var lifecycle: Observable<ActivityLifecycle>

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var config: TheActivityConfig

  @Inject
  lateinit var syncSetup: SyncSetup

  @Inject
  lateinit var unauthorizeUser: UnauthorizeUser

  @Inject
  lateinit var utcClock: UtcClock

  private val disposables = CompositeDisposable()

  private val screenRouter: ScreenRouter by unsafeLazy {
    ScreenRouter.create(this, NestedKeyChanger(), screenResults)
  }

  private val screenResults: ScreenResultBus = ScreenResultBus()

  @SuppressLint("CheckResult")
  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    @Suppress("ConstantConditionIf")
    if (BuildConfig.DISABLE_SCREENSHOT) {
      window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    if (savedInstanceState == null) {
      disposables.addAll(
          syncSetup.run(),
          unauthorizeUser.listen()
      )
    }

    lifecycle
        .startWith(Started(javaClass.simpleName))
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(lifecycle.ofType<Destroyed>())
        .subscribe { uiChange -> uiChange(this) }

    if (intent.hasExtra(EXTRA_DEEP_LINK_RESULT)) {
      when (val deepLinkResult = intent.getParcelableExtra<DeepLinkResult>(EXTRA_DEEP_LINK_RESULT)) {
        is OpenPatientSummary -> showPatientSummaryForDeepLink(deepLinkResult)
        is ShowPatientNotFound -> showPatientNotFoundErrorDialog()
        is ShowNoPatientUuid -> showNoPatientUuidErrorDialog()
      }
    }
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDiGraph()

    val wrappedContext = baseContext
        .wrap { LocaleOverrideContextWrapper.wrap(it, locale) }
        .wrap { wrapContextWithRouter(it) }
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
  }

  private fun wrapContextWithRouter(baseContext: Context): Context {
    screenRouter.registerKeyChanger(FullScreenKeyChanger(
        activity = this,
        screenLayoutContainerRes = android.R.id.content,
        screenBackgroundRes = R.color.window_background,
        onKeyChange = this::onScreenChanged
    ))
    return screenRouter.installInContext(baseContext, controller.initialScreenKey())
  }

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .theActivityComponentBuilder()
        .activity(this)
        .screenRouter(screenRouter)
        .build()
    component.inject(this)
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

  override fun onSaveInstanceState(outState: Bundle) {
    if (config.shouldLogSavedStateSizes) {
      screenRouter.logSizesOfSavedStates()
    }
    super.onSaveInstanceState(outState)
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.clear()
  }

  fun showAppLockScreen() {
    screenRouter.push(AppLockScreenKey())
  }

  // This is here because we need to show the same alert in multiple
  // screens when the user gets verified in the background.
  fun showUserLoggedOutOnOtherDeviceAlert() {
    LoggedOutOnOtherDeviceDialog.show(supportFragmentManager)
  }

  fun redirectToLogin() {
    screenRouter.clearHistoryAndPush(RegistrationPhoneScreenKey(), RouterDirection.REPLACE)
  }

  fun showAccessDeniedScreen(fullName: String) {
    screenRouter.clearHistoryAndPush(AccessDeniedScreenKey(fullName), RouterDirection.REPLACE)
  }

  private fun showPatientSummaryForDeepLink(deepLinkResult: OpenPatientSummary) {
    screenRouter
        .push(PatientSummaryScreenKey(
            patientUuid = deepLinkResult.patientUuid,
            intention = OpenIntention.ViewExistingPatient,
            screenCreatedTimestamp = Instant.now(utcClock)
        ))
  }

  private fun showPatientNotFoundErrorDialog() {
    AlertDialog.Builder(this, R.style.Clinic_V2_DialogStyle)
        .setTitle(R.string.deeplink_patient_profile_not_found)
        .setMessage(R.string.deeplink_patient_profile_not_found_desc)
        .setPositiveButton(R.string.deeplink_patient_profile_not_found_positive_action, null)
        .show()
  }

  private fun showNoPatientUuidErrorDialog() {
    AlertDialog.Builder(this, R.style.Clinic_V2_DialogStyle)
        .setTitle(R.string.deeplink_no_patient)
        .setMessage(R.string.deeplink_no_patient_desc)
        .setPositiveButton(R.string.deeplink_no_patient_positive_action, null)
        .show()
  }
}
