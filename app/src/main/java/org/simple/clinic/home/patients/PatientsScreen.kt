package org.simple.clinic.home.patients

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.IdRes
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.ViewFlipper
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.Disposables
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.enterotp.EnterOtpScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.PatientSearchScreen
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.indexOfChildId
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
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
  private val approvalStatusViewFlipper by bindView<ViewFlipper>(R.id.patients_user_status_viewflipper)
  private val dismissApprovedStatusButton by bindView<Button>(R.id.patients_dismiss_user_approved_status)
  private val enterOtpManuallyButton by bindView<Button>(R.id.patients_enter_code)
  private val nameInStatusSavedText by bindView<TextView>(R.id.patients_summary_saved_name)
  private val nameInAppointmentSavedText by bindView<TextView>(R.id.patients_summary_appointment_saved_name)
  private val dateInAppointmentSavedText by bindView<TextView>(R.id.patients_summary_appointment_saved_date)

  @IdRes
  private var currentStatusViewId: Int = R.id.patients_user_status_hidden
  private var disposable = Disposables.empty()

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    setupApprovalStatusAnimations()

    val screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }

    Observable
        .mergeArray(
            screenCreates(),
            screenDestroys,
            activityStarts(),
            searchButtonClicks(),
            dismissApprovedStatusClicks(),
            enterCodeManuallyClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(screenDestroys)
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

  private fun searchButtonClicks() = RxView.clicks(searchButton).map { NewPatientClicked }

  private fun dismissApprovedStatusClicks() = RxView.clicks(dismissApprovedStatusButton).map { UserApprovedStatusDismissed() }

  private fun enterCodeManuallyClicks() = RxView.clicks(enterOtpManuallyButton).map { PatientsEnterCodeManuallyClicked() }

  fun openNewPatientScreen() {
    screenRouter.push(PatientSearchScreen.KEY)
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
}
