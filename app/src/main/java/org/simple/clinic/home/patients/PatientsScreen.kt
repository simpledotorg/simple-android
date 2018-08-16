package org.simple.clinic.home.patients

import android.content.Context
import android.util.AttributeSet
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
import org.simple.clinic.widgets.setDisplayedChildId
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

  private val searchButton by bindView<Button>(R.id.patients_search_patients)
  private val aadhaarScanButton by bindView<Button>(R.id.patients_scan_aadhaar)
  private val approvalStatusViewFlipper by bindView<ViewFlipper>(R.id.patients_user_status_viewflipper)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    Observable.merge(screenCreates(), activityStarts(), aadhaarScanButtonClicks(), searchButtonClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun activityStarts() = activityLifecycle.ofType<TheActivityLifecycle.Resumed>()

  private fun aadhaarScanButtonClicks() = RxView.clicks(aadhaarScanButton).map { ScanAadhaarClicked() }

  private fun searchButtonClicks() = RxView.clicks(searchButton).map { NewPatientClicked() }

  fun openNewPatientScreen() {
    screenRouter.push(PatientSearchScreen.KEY)
  }

  fun showUserStatusAsWaiting() {
    approvalStatusViewFlipper.setDisplayedChildId(R.id.patients_user_status_awaitingapproval)
  }

  fun showUserStatusAsApproved() {
    approvalStatusViewFlipper.setDisplayedChildId(R.id.patients_user_status_approved)
  }

  fun hideUserApprovalStatus() {
    // By changing to an empty child instead of hiding the ViewFlipper entirely,
    // ViewFlipper's change animations can be re-used for this transition.
    approvalStatusViewFlipper.setDisplayedChildId(R.id.patients_user_status_hidden)
  }
}
