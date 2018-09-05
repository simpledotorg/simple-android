package org.simple.clinic.home.patients

import android.content.Context
import android.support.annotation.IdRes
import android.support.v4.app.FragmentManager
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.ViewFlipper
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.PatientSearchScreen
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.indexOfChildId
import javax.inject.Inject

open class PatientsScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientsScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientsScreenController

  @Inject
  lateinit var activityLifecycle: Observable<TheActivityLifecycle>

  @Inject
  lateinit var fragmentManager: FragmentManager

  private val searchButton by bindView<Button>(R.id.patients_search_patients)
  private val aadhaarScanButton by bindView<Button>(R.id.patients_scan_aadhaar)
  private val approvalStatusViewFlipper by bindView<ViewFlipper>(R.id.patients_user_status_viewflipper)
  private val dismissApprovedStatusButton by bindView<Button>(R.id.patients_dismiss_user_approved_status)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    setupApprovalStatusAnimations()

    Observable
        .mergeArray(
            screenCreates(),
            activityStarts(),
            aadhaarScanButtonClicks(),
            searchButtonClicks(),
            dismissApprovedStatusClicks()
        )
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun setupApprovalStatusAnimations() {
    val entryAnimation = AnimationUtils.loadAnimation(context, R.anim.user_approval_status_entry)
    approvalStatusViewFlipper.inAnimation = entryAnimation.apply { interpolator = FastOutSlowInInterpolator() }

    val exitAnimation = AnimationUtils.loadAnimation(context, R.anim.user_approval_status_exit)
    approvalStatusViewFlipper.outAnimation = exitAnimation.apply { interpolator = FastOutSlowInInterpolator() }
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun activityStarts() = activityLifecycle.ofType<TheActivityLifecycle.Resumed>()

  private fun aadhaarScanButtonClicks() = RxView.clicks(aadhaarScanButton).map { ScanAadhaarClicked() }

  private fun searchButtonClicks() = RxView.clicks(searchButton).map { NewPatientClicked() }

  private fun dismissApprovedStatusClicks() = RxView.clicks(dismissApprovedStatusButton).map { UserApprovedStatusDismissed() }

  fun openNewPatientScreen() {
    screenRouter.push(PatientSearchScreen.KEY)
  }

  private fun showUserApprovalStatus(@IdRes statusViewId: Int) {
    approvalStatusViewFlipper.apply {
      val statusViewIndex = indexOfChildId(statusViewId)

      // Avoid duplicate calls because ViewFlipper re-plays transition
      // animations even if the child-to-display is the same.
      if (displayedChild != statusViewIndex) {
        displayedChild = statusViewIndex
      }
    }
  }

  fun showUserStatusAsWaiting() {
    showUserApprovalStatus(R.id.patients_user_status_awaitingapproval)
  }

  fun showUserStatusAsApproved() {
    showUserApprovalStatus(R.id.patients_user_status_approved)
  }

  fun showUserStatusAsPendingVerification() {
    showUserApprovalStatus(R.id.patients_user_status_awaitingsmsverification)
  }

  fun hideUserApprovalStatus() {
    // By changing to an empty child instead of hiding the ViewFlipper entirely,
    // ViewFlipper's change animations can be re-used for this transition.
    showUserApprovalStatus(R.id.patients_user_status_hidden)
  }

  fun showUserVerifiedAlert() {
    LoggedOutOnOtherDeviceDialog.show(fragmentManager)
  }
}
