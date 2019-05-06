package org.simple.clinic.home.patients

import android.Manifest
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.annotation.IdRes
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.ofType
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
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
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.indexOfChildId
import org.simple.clinic.widgets.visibleOrGone
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
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
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var userClock: UserClock

  private val searchButton by bindView<Button>(R.id.patients_search_patients)
  private val approvalStatusViewFlipper by bindView<ViewFlipper>(R.id.patients_user_status_viewflipper)
  private val dismissApprovedStatusButton by bindView<Button>(R.id.patients_dismiss_user_approved_status)
  private val enterOtpManuallyButton by bindView<Button>(R.id.patients_enter_code)
  private val nameInStatusSavedText by bindView<TextView>(R.id.patients_summary_saved_name)
  private val nameInAppointmentSavedText by bindView<TextView>(R.id.patients_summary_appointment_saved_name)
  private val dateInAppointmentSavedText by bindView<TextView>(R.id.patients_summary_appointment_saved_date)
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
    val worldHypertensionDay = today.withMonth(Month.MAY.value).withDayOfMonth(17)
    val dateToShowHypertensionDayBannerFrom = worldHypertensionDay.minusDays(6)

    val fathersDay = today.withMonth(Month.JUNE.value).withDayOfMonth(17)
    val dateToShowFathersDayBannerFrom = fathersDay.minusDays(8)

    return when (today) {
      in dateToShowHypertensionDayBannerFrom..worldHypertensionDay -> R.drawable.ic_homescreen_world_hypertension_day
      in dateToShowFathersDayBannerFrom..fathersDay -> R.drawable.ic_homescreen_fathers_day
      else -> R.drawable.illustrations_homescreen
    }
  }

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

  private fun showSummaryStatus(@IdRes statusViewId: Int) {
    showStatus(statusViewId)
    disposable = Observable.timer(2500L, TimeUnit.MILLISECONDS, mainThread())
        .subscribe {
          showUserAccountStatus(currentStatusViewId)
        }
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

  fun showStatusPatientSummarySaved(patientName: String) {
    nameInStatusSavedText.text = patientName
    showSummaryStatus(R.id.patient_status_summary_saved)
  }

  fun showStatusPatientAppointmentSaved(patientName: String, appointmentDate: LocalDate) {
    nameInAppointmentSavedText.text = context.getString(R.string.patient_status_summary_saved_scheduled_appointment, patientName)
    dateInAppointmentSavedText.text = appointmentDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH))
    showSummaryStatus(R.id.patients_summary_appointment_saved)
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
}
