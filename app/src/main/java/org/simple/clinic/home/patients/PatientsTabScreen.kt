package org.simple.clinic.home.patients

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.activity.ActivityLifecycle
import org.simple.clinic.activity.ActivityLifecycle.Resumed
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appupdate.dialog.AppUpdateDialog
import org.simple.clinic.databinding.ScreenPatientsBinding
import org.simple.clinic.di.injector
import org.simple.clinic.enterotp.EnterOtpScreenKey
import org.simple.clinic.feature.Features
import org.simple.clinic.instantsearch.InstantSearchScreenKey
import org.simple.clinic.mobius.DeferredEventSource
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.scanid.OpenedFrom
import org.simple.clinic.scanid.ScanSimpleIdScreenKey
import org.simple.clinic.shortcodesearchresult.ShortCodeSearchResultScreenKey
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.RequestPermissions
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.indexOfChildId
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class PatientsTabScreen : BaseScreen<
    PatientsTabScreen.Key,
    ScreenPatientsBinding,
    PatientsTabModel,
    PatientsTabEvent,
    PatientsTabEffect>(), PatientsTabUi, PatientsTabUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenResults: ScreenResultBus

  @Inject
  lateinit var activityLifecycle: Observable<ActivityLifecycle>

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var crashReporter: CrashReporter

  @Inject
  lateinit var country: Country

  @Inject
  @Named("training_video_youtube_id")
  lateinit var youTubeVideoId: String

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  @Inject
  lateinit var effectHandlerFactory: PatientsEffectHandler.Factory

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var features: Features

  private val deferredEvents = DeferredEventSource<PatientsTabEvent>()

  @IdRes
  private var currentStatusViewId: Int = R.id.userStatusHiddenView

  private val homeIllustration
    get() = binding.homeIllustration

  private val userStatusViewflipper
    get() = binding.userStatusViewflipper

  private val searchPatientsButton
    get() = binding.searchPatientsButton

  private val userStatusApproved
    get() = binding.userStatusApproved

  private val dismissApprovedStatusButton
    get() = userStatusApproved.dismissApprovedStatusButton

  private val userAwaitingSmsVerification
    get() = binding.userAwaitingSmsVerification

  private val enterCodeButton
    get() = userAwaitingSmsVerification.enterCodeButton

  private val scanSimpleCardButton
    get() = binding.scanSimpleCardButton

  private val simpleVideoLayout
    get() = binding.simpleVideoLayout

  private val videoTitleText
    get() = simpleVideoLayout.videoTitleText

  private val simpleVideoImage
    get() = simpleVideoLayout.simpleVideoImage

  private val simpleVideoDuration
    get() = simpleVideoLayout.simpleVideoDuration

  private val syncIndicator
    get() = binding.syncIndicator

  private val illustrationLayout
    get() = binding.illustrationLayout

  override fun defaultModel() = PatientsTabModel.create()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenPatientsBinding.inflate(layoutInflater, container, false)

  override fun uiRenderer() = PatientsTabUiRenderer(this)

  override fun events() = Observable
      .mergeArray(
          activityResumes(),
          searchButtonClicks(),
          dismissApprovedStatusClicks(),
          enterCodeManuallyClicks(),
          scanCardIdButtonClicks(),
          simpleVideoClicked()
      )
      .compose<UiEvent>(RequestPermissions(runtimePermissions, screenResults.streamResults().ofType()))
      .compose(ReportAnalyticsEvents())
      .cast<PatientsTabEvent>()

  override fun createUpdate() = PatientsTabUpdate()

  override fun createInit() = PatientsInit()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun additionalEventSources() = listOf(deferredEvents)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupApprovalStatusAnimations()

    homeIllustration.setImageResource(illustrationResourceId())
  }

  private fun illustrationResourceId(): Int =
      when (country.isoCountryCode) {
        Country.INDIA -> R.drawable.ic_homescreen_india
        Country.BANGLADESH -> R.drawable.ic_homescreen_bangladesh
        Country.ETHIOPIA -> R.drawable.ic_homescreen_ethiopia
        else -> R.drawable.ic_homescreen_default
      }

  private fun setupApprovalStatusAnimations() {
    val entryAnimation = AnimationUtils.loadAnimation(context, R.anim.user_approval_status_entry)
    userStatusViewflipper.inAnimation = entryAnimation.apply { interpolator = FastOutSlowInInterpolator() }

    val exitAnimation = AnimationUtils.loadAnimation(context, R.anim.user_approval_status_exit)
    userStatusViewflipper.outAnimation = exitAnimation.apply { interpolator = FastOutSlowInInterpolator() }
  }

  private fun activityResumes() = activityLifecycle
      .ofType<Resumed>()
      .map { ActivityResumed }

  private fun searchButtonClicks() = searchPatientsButton.clicks().map { NewPatientClicked }

  private fun dismissApprovedStatusClicks() = dismissApprovedStatusButton.clicks().map { UserApprovedStatusDismissed() }

  private fun enterCodeManuallyClicks() = enterCodeButton.clicks().map { PatientsEnterCodeManuallyClicked() }

  private fun scanCardIdButtonClicks() = scanSimpleCardButton.clicks().map { ScanCardIdButtonClicked() }

  private fun simpleVideoClicked() = videoTitleText.clicks()
      .mergeWith(simpleVideoImage.clicks())
      .map { SimpleVideoClicked }

  override fun openPatientSearchScreen(additionalIdentifier: Identifier?) {
    val screenKey = InstantSearchScreenKey(
        additionalIdentifier = additionalIdentifier,
        initialSearchQuery = null)

    router.push(screenKey)
  }

  private fun showStatus(@IdRes statusViewId: Int) {
    userStatusViewflipper.apply {
      val statusViewIndex = indexOfChildId(statusViewId)

      // Avoid duplicate calls because ViewFlipper re-plays transition
      // animations even if the child-to-display is the same.
      if (displayedChild != statusViewIndex) {
        displayedChild = statusViewIndex
      }
    }
  }

  private fun showUserAccountStatus(@IdRes statusViewId: Int) {
    showStatus(statusViewId)
    currentStatusViewId = userStatusViewflipper.currentView.id
  }

  override fun showUserStatusAsWaiting() {
    showUserAccountStatus(R.id.userStatusAwaitingApproval)
  }

  override fun showUserStatusAsApproved() {
    showUserAccountStatus(R.id.userStatusApproved)
  }

  override fun showUserStatusAsPendingVerification() {
    showUserAccountStatus(R.id.userAwaitingSmsVerification)
  }

  override fun hideUserAccountStatus() {
    // By changing to an empty child instead of hiding the ViewFlipper entirely,
    // ViewFlipper's change animations can be re-used for this transition.
    showUserAccountStatus(R.id.userStatusHiddenView)
  }

  override fun openEnterCodeManuallyScreen() {
    router.push(EnterOtpScreenKey().wrap())
  }

  override fun openScanSimpleIdCardScreen() {
    router.push(ScanSimpleIdScreenKey(OpenedFrom.PatientsTabScreen))
  }

  override fun hideSyncIndicator() {
    syncIndicator.visibility = View.GONE
  }

  override fun showSyncIndicator() {
    syncIndicator.visibility = View.VISIBLE
  }

  override fun showAppUpdateDialog() {
    AppUpdateDialog.show(activity.supportFragmentManager)
  }

  override fun openShortCodeSearchScreen(shortCode: String) {
    router.push(ShortCodeSearchResultScreenKey(shortCode))
  }

  override fun openPatientSummary(patientId: UUID) {
    router.push(PatientSummaryScreenKey(patientId, OpenIntention.ViewExistingPatient, Instant.now(utcClock)))
  }

  private fun showHomeScreenBackground(@IdRes viewId: Int) {
    illustrationLayout.apply {
      displayedChild = indexOfChildId(viewId)
    }
    illustrationLayout.visibility = View.VISIBLE
  }

  override fun showSimpleVideo() {
    // Hard-coding to show this simple video view exists because, as of now,
    // we are not sure if we will have variations of this training video.
    // We should make the title, duration and video thumbnail configurable in order to improve this.
    simpleVideoDuration.text = resources.getString(R.string.simple_video_duration, "5:07")
    showHomeScreenBackground(R.id.simpleVideoLayout)
  }

  override fun showIllustration() {
    showHomeScreenBackground(R.id.homeIllustration)
  }

  override fun openYouTubeLinkForSimpleVideo() {
    val packageManager = requireContext().packageManager
    val appUri = "vnd.youtube:$youTubeVideoId"
    val webUri = "http://www.youtube.com/watch?v=$youTubeVideoId"

    val resolvedIntent = listOf(appUri, webUri)
        .map { Uri.parse(it) }
        .map { Intent(Intent.ACTION_VIEW, it) }
        .firstOrNull { it.resolveActivity(packageManager) != null }

    if (resolvedIntent != null) {
      requireContext().startActivity(resolvedIntent)
    } else {
      crashReporter.report(ActivityNotFoundException("Unable to play simple video because no supporting apps were found."))
    }
  }

  interface Injector {
    fun inject(target: PatientsTabScreen)
  }

  @Parcelize
  class Key : ScreenKey() {

    override val analyticsName = "Patients"

    override fun instantiateFragment() = PatientsTabScreen()
  }
}
