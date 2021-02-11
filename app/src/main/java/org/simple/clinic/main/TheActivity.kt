package org.simple.clinic.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.simple.clinic.BuildConfig
import org.simple.clinic.ClinicApp
import org.simple.clinic.R
import org.simple.clinic.deeplink.DeepLinkResult
import org.simple.clinic.deeplink.OpenPatientSummary
import org.simple.clinic.deeplink.OpenPatientSummaryWithTeleconsultLog
import org.simple.clinic.deeplink.ShowNoPatientUuid
import org.simple.clinic.deeplink.ShowPatientNotFound
import org.simple.clinic.deeplink.ShowTeleconsultNotAllowed
import org.simple.clinic.deniedaccess.AccessDeniedScreenKey
import org.simple.clinic.di.InjectorProviderContextWrapper
import org.simple.clinic.empty.EmptyScreenKey
import org.simple.clinic.feature.Features
import org.simple.clinic.forgotpin.createnewpin.ForgotPinCreateNewPinScreenKey
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.home.patients.LoggedOutOnOtherDeviceDialog
import org.simple.clinic.login.applock.AppLockConfig
import org.simple.clinic.login.applock.AppLockScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.registerorlogin.AuthenticationActivity
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.storage.MemoryValue
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.SyncSetup
import org.simple.clinic.user.UnauthorizeUser
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.OTP_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.UNAUTHORIZED
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.disableAnimations
import org.simple.clinic.util.finishWithoutAnimations
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.util.withLocale
import org.simple.clinic.util.wrap
import java.time.Instant
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun initialScreenKey(
    user: User
): ScreenKey {
  val userDisapproved = user.status == UserStatus.DisapprovedForSyncing

  val canMoveToHomeScreen = when (user.loggedInStatus) {
    RESETTING_PIN -> false
    LOGGED_IN, OTP_REQUESTED, RESET_PIN_REQUESTED, UNAUTHORIZED -> true
  }

  return when {
    canMoveToHomeScreen && !userDisapproved -> HomeScreenKey
    userDisapproved -> AccessDeniedScreenKey(user.fullName)
    user.loggedInStatus == RESETTING_PIN -> ForgotPinCreateNewPinScreenKey().wrap()
    else -> throw IllegalStateException("Unknown user status combinations: [${user.loggedInStatus}, ${user.status}]")
  }
}

class TheActivity : AppCompatActivity(), TheActivityUi {

  companion object {
    private const val EXTRA_DEEP_LINK_RESULT = "TheActivity.EXTRA_DEEP_LINK_RESULT"
    private const val EXTRA_IS_FRESH_AUTHENTICATION = "TheActivity.EXTRA_IS_FRESH_AUTHENTICATION"

    fun newIntent(
        context: Context,
        isFreshAuthentication: Boolean
    ): Intent {
      return Intent(context, TheActivity::class.java).apply {
        putExtra(EXTRA_IS_FRESH_AUTHENTICATION, isFreshAuthentication)
      }
    }

    fun intentForOpenPatientSummary(context: Context, patientUuid: UUID): Intent {
      return Intent(context, TheActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(EXTRA_DEEP_LINK_RESULT, OpenPatientSummary(patientUuid))
      }
    }

    fun intentForOpenPatientSummaryWithTeleconsultLog(context: Context, patientUuid: UUID, teleconsultRecordId: UUID): Intent {
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

    fun intentForShowTeleconsultNotAllowedError(context: Context): Intent {
      return Intent(context, TheActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(EXTRA_DEEP_LINK_RESULT, ShowTeleconsultNotAllowed)
      }
    }
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

  @Inject
  lateinit var unlockAfterTimestamp: MemoryValue<Optional<Instant>>

  @Inject
  lateinit var dataSync: DataSync

  private lateinit var component: TheActivityComponent

  private val disposables = CompositeDisposable()

  private val router by unsafeLazy {
    Router(
        initialScreenKey = EmptyScreenKey().wrap(),
        fragmentManager = supportFragmentManager,
        containerId = android.R.id.content
    )
  }

  private val screenResults: ScreenResultBus = ScreenResultBus()

  private val isFreshAuthentication: Boolean by unsafeLazy {
    intent.getBooleanExtra(EXTRA_IS_FRESH_AUTHENTICATION, false)
  }

  private val delegate by unsafeLazy {
    val uiRenderer = TheActivityUiRenderer(this)

    val defaultModel = if (isFreshAuthentication)
      TheActivityModel.createForNewlyLoggedInUser()
    else
      TheActivityModel.createForAlreadyLoggedInUser()

    MobiusDelegate.forActivity(
        events = Observable.never(),
        defaultModel = defaultModel,
        update = TheActivityUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = TheActivityInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    router.onReady(savedInstanceState)
    delegate.onRestoreInstanceState(savedInstanceState)

    if (savedInstanceState == null) {
      loadInitialScreen()
    }
  }

  private fun loadInitialScreen() {
    val currentUser = userSession.loggedInUser().blockingFirst().get()

    val initialScreen = initialScreenKey(currentUser)

    router.clearHistoryAndPush(initialScreen)
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
      dataSync.fireAndForgetSync()
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
      is ShowTeleconsultNotAllowed -> showTeleconsultNotAllowedErrorDialog()
    }
    intent.removeExtra(EXTRA_DEEP_LINK_RESULT)
  }

  override fun attachBaseContext(baseContext: Context) {
    setupDiGraph()

    val wrappedContext = baseContext
        .wrap { InjectorProviderContextWrapper.wrap(it, component) }
        .wrap { ViewPumpContextWrapper.wrap(it) }

    super.attachBaseContext(wrappedContext)
    applyOverrideConfiguration(Configuration())
  }

  override fun applyOverrideConfiguration(overrideConfiguration: Configuration) {
    super.applyOverrideConfiguration(overrideConfiguration.withLocale(locale, features))
  }

  override fun onStart() {
    super.onStart()
    delegate.start()
  }

  override fun onStop() {
    if (!router.hasKeyOfType(AppLockScreenKey::class.java)) {
      val lockAfterTimestamp = Instant.now(utcClock).plusMillis(config.lockAfterTimeMillis)
      unlockAfterTimestamp.set(Optional.of(lockAfterTimestamp))
    }

    delegate.stop()
    super.onStop()
  }

  private fun setupDiGraph() {
    component = ClinicApp.appComponent
        .theActivityComponent()
        .create(
            activity = this,
            router = router,
            screenResults = screenResults
        )

    component.inject(this)
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
    if (!router.onBackPressed()) {
      super.onBackPressed()
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    router.onSaveInstanceState(outState)
    delegate.onSaveInstanceState(outState)
    super.onSaveInstanceState(outState)
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.clear()
  }

  override fun showAppLockScreen() {
    router.push(AppLockScreenKey.wrap())
  }

  // This is here because we need to show the same alert in multiple
  // screens when the user gets verified in the background.
  override fun showUserLoggedOutOnOtherDeviceAlert() {
    LoggedOutOnOtherDeviceDialog.show(supportFragmentManager)
  }

  override fun redirectToLogin() {
    val intent = AuthenticationActivity
        .forReauthentication(this)
        .disableAnimations()

    startActivity(intent)
    finishWithoutAnimations()
  }

  override fun showAccessDeniedScreen(fullName: String) {
    router.clearHistoryAndPush(AccessDeniedScreenKey(fullName))
  }

  private fun showPatientSummaryForDeepLink(deepLinkResult: OpenPatientSummary) {
    router.push(
        PatientSummaryScreenKey(
            patientUuid = deepLinkResult.patientUuid,
            intention = OpenIntention.ViewExistingPatient,
            screenCreatedTimestamp = Instant.now(utcClock)
        )
    )
  }

  private fun showPatientSummaryWithTeleconsultLogForDeepLink(deepLinkResult: OpenPatientSummaryWithTeleconsultLog) {
    router.push(
        PatientSummaryScreenKey(
            patientUuid = deepLinkResult.patientUuid,
            intention = OpenIntention.ViewExistingPatientWithTeleconsultLog(deepLinkResult.teleconsultRecordId),
            screenCreatedTimestamp = Instant.now(utcClock)
        )
    )
  }

  private fun showPatientNotFoundErrorDialog() {
    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.deeplink_patient_profile_not_found)
        .setMessage(R.string.deeplink_patient_profile_not_found_desc)
        .setPositiveButton(R.string.deeplink_patient_profile_not_found_positive_action, null)
        .show()
  }

  private fun showNoPatientUuidErrorDialog() {
    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.deeplink_no_patient)
        .setMessage(R.string.deeplink_no_patient_desc)
        .setPositiveButton(R.string.deeplink_no_patient_positive_action, null)
        .show()
  }


  private fun showTeleconsultNotAllowedErrorDialog() {
    MaterialAlertDialogBuilder(this)
        .setTitle(R.string.deeplink_medical_officer_not_authorised_to_log_teleconsult)
        .setMessage(R.string.deeplink_please_check_with_your_supervisor)
        .setPositiveButton(R.string.deeplink_okay_positive_action, null)
        .show()
  }
}
