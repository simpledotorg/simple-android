package org.simple.clinic.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.simple.clinic.BuildConfig
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.deeplink.DeepLinkResult
import org.simple.clinic.deeplink.OpenPatientSummary
import org.simple.clinic.deeplink.OpenPatientSummaryWithTeleconsultLog
import org.simple.clinic.deeplink.ShowNoPatientUuid
import org.simple.clinic.deeplink.ShowPatientNotFound
import org.simple.clinic.deniedaccess.AccessDeniedScreenKey
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.feature.Feature.LogSavedStateSizes
import org.simple.clinic.feature.Features
import org.simple.clinic.forgotpin.createnewpin.ForgotPinCreateNewPinScreenKey
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.home.patients.LoggedOutOnOtherDeviceDialog
import org.simple.clinic.login.applock.AppLockConfig
import org.simple.clinic.login.applock.AppLockScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.platform.analytics.Analytics
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
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.LocaleOverrideContextWrapper
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.toNullable
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.wrap
import java.time.Instant
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun initialScreenKey(
    user: User?
): FullScreenKey {
  val userDisapproved = user?.status == UserStatus.DisapprovedForSyncing

  val canMoveToHomeScreen = when (user?.loggedInStatus) {
    User.LoggedInStatus.NOT_LOGGED_IN, User.LoggedInStatus.RESETTING_PIN, User.LoggedInStatus.UNAUTHORIZED -> false
    User.LoggedInStatus.LOGGED_IN, User.LoggedInStatus.OTP_REQUESTED, User.LoggedInStatus.RESET_PIN_REQUESTED -> true
    null -> false
  }

  return when {
    canMoveToHomeScreen && !userDisapproved -> HomeScreenKey
    userDisapproved -> AccessDeniedScreenKey(user?.fullName!!)
    user?.loggedInStatus == User.LoggedInStatus.RESETTING_PIN -> ForgotPinCreateNewPinScreenKey()
    else -> RegistrationPhoneScreenKey()
  }
}

class TheActivity : AppCompatActivity(), TheActivityUi {

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

    fun intentForOpenPatientSummaryWithTeleconsultLog(context: Context, patientUuid: UUID, teleconsultRecordId: UUID?): Intent {
      return Intent(context, TheActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(EXTRA_DEEP_LINK_RESULT, OpenPatientSummaryWithTeleconsultLog(patientUuid, teleconsultRecordId))
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
  lateinit var locale: Locale

  @Inject
  lateinit var syncSetup: SyncSetup

  @Inject
  lateinit var unauthorizeUser: UnauthorizeUser

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var effectHandlerFactory: TheActivityEffectHandler.InjectionFactory

  @Inject
  lateinit var config: AppLockConfig

  private val lifecycleEvents: Subject<LifecycleEvent> = PublishSubject.create()

  private val disposables = CompositeDisposable()

  private val screenRouter: ScreenRouter by unsafeLazy {
    ScreenRouter.create(this, NestedKeyChanger(), screenResults)
  }

  private val screenResults: ScreenResultBus = ScreenResultBus()

  private val events by unsafeLazy {
    lifecycleEvents
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate by unsafeLazy {
    val uiRenderer = TheActivityUiRenderer(this)

    MobiusDelegate.forActivity(
        events = events.ofType(),
        defaultModel = TheActivityModel.create(),
        update = TheActivityUpdate.create(config),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = TheActivityInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    delegate.onRestoreInstanceState(savedInstanceState)
  }

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

    if (intent.hasExtra(EXTRA_DEEP_LINK_RESULT)) {
      handleDeepLinkResult()
    }
  }

  private fun handleDeepLinkResult() {
    when (val deepLinkResult = intent.getParcelableExtra<DeepLinkResult>(EXTRA_DEEP_LINK_RESULT)) {
      is OpenPatientSummary -> showPatientSummaryForDeepLink(deepLinkResult)
      is ShowPatientNotFound -> showPatientNotFoundErrorDialog()
      is ShowNoPatientUuid -> showNoPatientUuidErrorDialog()
      is OpenPatientSummaryWithTeleconsultLog -> showPatientSummaryWithTeleconsultLogForDeepLink(deepLinkResult)
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

  override fun onStart() {
    super.onStart()
    delegate.start()
    lifecycleEvents.onNext(LifecycleEvent.ActivityStarted)
  }

  override fun onStop() {
    lifecycleEvents.onNext(LifecycleEvent.ActivityStopped(Instant.now(utcClock)))
    delegate.stop()
    super.onStop()
  }

  private fun wrapContextWithRouter(baseContext: Context): Context {
    screenRouter.registerKeyChanger(FullScreenKeyChanger(
        activity = this,
        screenLayoutContainerRes = android.R.id.content,
        screenBackgroundRes = R.color.window_background,
        onKeyChange = this::onScreenChanged
    ))

    val currentUser: User? = userSession.loggedInUser().blockingFirst().toNullable()
    return screenRouter.installInContext(baseContext, initialScreenKey(currentUser))
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
    if (features.isEnabled(LogSavedStateSizes)) {
      screenRouter.logSizesOfSavedStates()
    }
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun onDestroy() {
    super.onDestroy()
    lifecycleEvents.onNext(LifecycleEvent.ActivityDestroyed)
    disposables.clear()
  }

  override fun showAppLockScreen() {
    screenRouter.push(AppLockScreenKey())
  }

  // This is here because we need to show the same alert in multiple
  // screens when the user gets verified in the background.
  override fun showUserLoggedOutOnOtherDeviceAlert() {
    LoggedOutOnOtherDeviceDialog.show(supportFragmentManager)
  }

  override fun redirectToLogin() {
    screenRouter.clearHistoryAndPush(RegistrationPhoneScreenKey(), RouterDirection.REPLACE)
  }

  override fun showAccessDeniedScreen(fullName: String) {
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

  private fun showPatientSummaryWithTeleconsultLogForDeepLink(deepLinkResult: OpenPatientSummaryWithTeleconsultLog) {
    screenRouter
        .push(PatientSummaryScreenKey(
            patientUuid = deepLinkResult.patientUuid,
            intention = OpenIntention.ViewExistingPatientWithTeleconsultLog(deepLinkResult.teleconsultRecordId),
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
