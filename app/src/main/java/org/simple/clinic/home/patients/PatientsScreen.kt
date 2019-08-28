package org.simple.clinic.home.patients

import android.Manifest
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ViewFlipper
import androidx.annotation.IdRes
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.ofType
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.activity.TheActivityLifecycle
import org.simple.clinic.appupdate.dialog.AppUpdateDialog
import org.simple.clinic.bindUiToController
import org.simple.clinic.enterotp.EnterOtpScreenKey
import org.simple.clinic.router.screen.ActivityPermissionResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.scanid.ScanSimpleIdScreenKey
import org.simple.clinic.search.PatientSearchScreenKey
import org.simple.clinic.sync.indicator.SyncIndicatorView
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.indexOfChildId
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import javax.inject.Inject

private const val REQUESTCODE_CAMERA_PERMISSION = 0
private const val CAMERA_PERMISSION = Manifest.permission.CAMERA

open class PatientsScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientsScreenController

  @Inject
  lateinit var activityLifecycle: Observable<TheActivityLifecycle>

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var userClock: UserClock

  private val searchButton by bindView<Button>(R.id.patients_search_patients)
  private val approvalStatusViewFlipper by bindView<ViewFlipper>(R.id.patients_user_status_viewflipper)
  private val dismissApprovedStatusButton by bindView<Button>(R.id.patients_dismiss_user_approved_status)
  private val enterOtpManuallyButton by bindView<Button>(R.id.patients_enter_code)
  private val scanSimpleCardButton by bindView<Button>(R.id.patients_scan_simple_card)
  private val syncIndicatorView by bindView<SyncIndicatorView>(R.id.patients_sync_indicator)
  private val illustrationImageView by bindView<ImageView>(R.id.patients_record_bp_illustration)

  @IdRes
  private var currentStatusViewId: Int = R.id.patients_user_status_hidden
  private var disposable = Disposables.empty()

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    setupApprovalStatusAnimations()

    bindUiToController(
        ui = this,
        events = Observable.mergeArray(
            screenCreates(),
            activityStarts(),
            searchButtonClicks(),
            dismissApprovedStatusClicks(),
            enterCodeManuallyClicks(),
            scanCardIdButtonClicks(),
            cameraPermissionChanges()),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )

    illustrationImageView.setImageResource(illustrationResourceId())
  }

  private fun illustrationResourceId(): Int {
    val today = LocalDate.now(userClock)

    val heartDay = today.withMonth(Month.SEPTEMBER.value).withDayOfMonth(29)
    val gandhiJayanti = today.withMonth(Month.OCTOBER.value).withDayOfMonth(2)
    val diwaliDay = today.withMonth(Month.OCTOBER.value).withDayOfMonth(27)

    return when (today) {
      in withinWeek(heartDay) -> R.drawable.ic_homescreen_heart_day_29_sep
      in withinWeek(gandhiJayanti) -> R.drawable.ic_homescreen_gandhi_jayanti_2_oct
      in withinWeek(diwaliDay) -> R.drawable.ic_homescreen_diwali_27_oct
      else -> R.drawable.ic_homescreen_default
    }
  }

  private fun withinWeek(date: LocalDate): ClosedRange<LocalDate> =
      date.minusDays(8)..date

  private fun setupApprovalStatusAnimations() {
    val entryAnimation = AnimationUtils.loadAnimation(context, R.anim.user_approval_status_entry)
    approvalStatusViewFlipper.inAnimation = entryAnimation.apply { interpolator = FastOutSlowInInterpolator() }

    val exitAnimation = AnimationUtils.loadAnimation(context, R.anim.user_approval_status_exit)
    approvalStatusViewFlipper.outAnimation = exitAnimation.apply { interpolator = FastOutSlowInInterpolator() }
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun activityStarts() = activityLifecycle.ofType<TheActivityLifecycle.Resumed>()

  private fun searchButtonClicks() = RxView.clicks(searchButton).map { NewPatientClicked }

  private fun dismissApprovedStatusClicks() = RxView.clicks(dismissApprovedStatusButton).map { UserApprovedStatusDismissed() }

  private fun enterCodeManuallyClicks() = RxView.clicks(enterOtpManuallyButton).map { PatientsEnterCodeManuallyClicked() }

  private fun scanCardIdButtonClicks() = RxView.clicks(scanSimpleCardButton).map { ScanCardIdButtonClicked }

  fun openPatientSearchScreen() {
    screenRouter.push(PatientSearchScreenKey())
  }

  private fun showStatus(@IdRes statusViewId: Int) {
    approvalStatusViewFlipper.apply {
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
    currentStatusViewId = approvalStatusViewFlipper.currentView.id
  }

  private fun cameraPermissionChanges(): Observable<UiEvent> {
    return screenRouter.streamScreenResults()
        .ofType<ActivityPermissionResult>()
        .filter { result -> result.requestCode == REQUESTCODE_CAMERA_PERMISSION }
        .map { RuntimePermissions.check(activity, CAMERA_PERMISSION) }
        .map(::PatientsScreenCameraPermissionChanged)
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    disposable.dispose()
  }

  fun showUserStatusAsWaiting() {
    showUserAccountStatus(R.id.patients_user_status_awaitingapproval)
  }

  fun showUserStatusAsApproved() {
    showUserAccountStatus(R.id.patients_user_status_approved)
  }

  fun showUserStatusAsPendingVerification() {
    showUserAccountStatus(R.id.patients_user_status_awaitingsmsverification)
  }

  fun hideUserAccountStatus() {
    // By changing to an empty child instead of hiding the ViewFlipper entirely,
    // ViewFlipper's change animations can be re-used for this transition.
    showUserAccountStatus(R.id.patients_user_status_hidden)
  }

  fun openEnterCodeManuallyScreen() {
    screenRouter.push(EnterOtpScreenKey())
  }

  fun setScanCardButtonEnabled(enabled: Boolean) {
    scanSimpleCardButton.visibleOrGone(enabled)
  }

  fun openScanSimpleIdCardScreen() {
    screenRouter.push(ScanSimpleIdScreenKey())
  }

  fun requestCameraPermissions() {
    RuntimePermissions.request(activity, CAMERA_PERMISSION, REQUESTCODE_CAMERA_PERMISSION)
  }

  fun hideSyncIndicator() {
    syncIndicatorView.visibility = View.GONE
  }

  fun showSyncIndicator() {
    syncIndicatorView.visibility = View.VISIBLE
  }

  fun showAppUpdateDialog() {
    AppUpdateDialog.show(activity.supportFragmentManager)
  }
}
