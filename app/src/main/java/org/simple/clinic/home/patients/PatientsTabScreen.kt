package org.simple.clinic.home.patients

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.patients_user_status_approved.view.*
import kotlinx.android.synthetic.main.patients_user_status_awaitingsmsverification.view.*
import kotlinx.android.synthetic.main.screen_patients.view.*
import kotlinx.android.synthetic.main.view_simple_video.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.activity.ActivityLifecycle
import org.simple.clinic.activity.ActivityLifecycle.Resumed
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appupdate.dialog.AppUpdateDialog
import org.simple.clinic.di.injector
import org.simple.clinic.enterotp.EnterOtpScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.scanid.ScanSimpleIdScreenKey
import org.simple.clinic.search.PatientSearchScreenKey
import org.simple.clinic.shortcodesearchresult.ShortCodeSearchResultScreenKey
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.RequestPermissions
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.indexOfChildId
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Named

class PatientsTabScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), PatientsTabUi, PatientsTabUiActions {

  @Inject
  lateinit var screenRouter: ScreenRouter

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

  @IdRes
  private var currentStatusViewId: Int = R.id.userStatusHiddenView

  private val events: Observable<UiEvent> by unsafeLazy {
    Observable
        .mergeArray(
            activityResumes(),
            searchButtonClicks(),
            dismissApprovedStatusClicks(),
            enterCodeManuallyClicks(),
            scanCardIdButtonClicks(),
            simpleVideoClicked()
        )
        .compose<UiEvent>(RequestPermissions(runtimePermissions, screenRouter.streamScreenResults().ofType()))
        .compose(ReportAnalyticsEvents())
  }

  private val delegate: MobiusDelegate<PatientsTabModel, PatientsTabEvent, PatientsTabEffect> by unsafeLazy {
    val uiRenderer = PatientsTabUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = PatientsTabModel.create(),
        update = PatientsTabUpdate(),
        init = PatientsInit(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)

    setupApprovalStatusAnimations()

    homeIllustration.setImageResource(illustrationResourceId())
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
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

  override fun openPatientSearchScreen() {
    screenRouter.push(PatientSearchScreenKey(additionalIdentifier = null))
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
    showUserAccountStatus(R.id.patients_user_status_awaitingapproval)
  }

  override fun showUserStatusAsApproved() {
    showUserAccountStatus(R.id.patients_user_status_approved)
  }

  override fun showUserStatusAsPendingVerification() {
    showUserAccountStatus(R.id.patients_user_status_awaitingsmsverification)
  }

  override fun hideUserAccountStatus() {
    // By changing to an empty child instead of hiding the ViewFlipper entirely,
    // ViewFlipper's change animations can be re-used for this transition.
    showUserAccountStatus(R.id.userStatusHiddenView)
  }

  override fun openEnterCodeManuallyScreen() {
    screenRouter.push(EnterOtpScreenKey())
  }

  override fun openScanSimpleIdCardScreen() {
    screenRouter.push(ScanSimpleIdScreenKey())
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
    screenRouter.push(ShortCodeSearchResultScreenKey(shortCode))
  }

  override fun openPatientSummary(patientId: UUID) {
    screenRouter.push(PatientSummaryScreenKey(patientId, OpenIntention.ViewExistingPatient, Instant.now(utcClock)))
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
    showHomeScreenBackground(simpleVideoLayout.id)
  }

  override fun showIllustration() {
    showHomeScreenBackground(homeIllustration.id)
  }

  override fun openYouTubeLinkForSimpleVideo() {
    val packageManager = context.packageManager
    val appUri = "vnd.youtube:$youTubeVideoId"
    val webUri = "http://www.youtube.com/watch?v=$youTubeVideoId"

    val resolvedIntent = listOf(appUri, webUri)
        .map { Uri.parse(it) }
        .map { Intent(Intent.ACTION_VIEW, it) }
        .firstOrNull { it.resolveActivity(packageManager) != null }

    if (resolvedIntent != null) {
      context.startActivity(resolvedIntent)
    } else {
      crashReporter.report(ActivityNotFoundException("Unable to play simple video because no supporting apps were found."))
    }
  }

  interface Injector {
    fun inject(target: PatientsTabScreen)
  }
}
