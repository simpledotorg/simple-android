package org.simple.clinic.login.applock

import android.content.Context
import android.support.transition.Fade
import android.support.transition.TransitionManager
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.facility.change.FacilityChangeScreen
import org.simple.clinic.router.screen.BackPressInterceptCallback
import org.simple.clinic.router.screen.BackPressInterceptor
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.StaggeredEditText
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class AppLockScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = AppLockScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: AppLockScreenController

  @Inject
  lateinit var activity: TheActivity

  private val facilityButton by bindView<Button>(R.id.applock_facility_name)
  private val fullNameTextView by bindView<TextView>(R.id.applock_user_fullname)
  private val logoutButton by bindView<Button>(R.id.applock_logout)
  private val pinEditText by bindView<StaggeredEditText>(R.id.applock_pin)
  private val pinFormLayout by bindView<LinearLayout>(R.id.applock_pin_container)
  private val progressView by bindView<ProgressBar>(R.id.applock_progress)
  private val errorTextView by bindView<TextView>(R.id.applock_error)
  private val forgotPinButton by bindView<Button>(R.id.applock_forgotpin)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable.mergeArray(screenCreates(), pinTextChanges(), submitClicks(), backClicks(), facilityClicks(), forgotPinClicks())
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }

    logoutButton.setOnClickListener {
      Toast.makeText(context, "Work in progress", Toast.LENGTH_SHORT).show()
    }

    // The keyboard shows up on PIN field automatically when the app is
    // starting, but not when the user comes back from FacilityChangeScreen.
    pinEditText.showKeyboard()
  }

  private fun screenCreates() = Observable.just(AppLockScreenCreated())

  private fun facilityClicks() = RxView.clicks(facilityButton).map { AppLockFacilityClicked() }

  private fun pinTextChanges() =
      pinEditText.textChanges()
          .map(CharSequence::toString)
          .map(::AppLockScreenPinTextChanged)

  private fun submitClicks() =
      pinEditText.textChanges()
          .filter { it.length == 4 }
          .map { AppLockScreenSubmitClicked() }

  private fun backClicks(): Observable<AppLockScreenBackClicked> {
    return Observable.create { emitter ->
      val interceptor = object : BackPressInterceptor {
        override fun onInterceptBackPress(callback: BackPressInterceptCallback) {
          emitter.onNext(AppLockScreenBackClicked())
          callback.markBackPressIntercepted()
        }
      }
      emitter.setCancellable { screenRouter.unregisterBackPressInterceptor(interceptor) }
      screenRouter.registerBackPressInterceptor(interceptor)
    }
  }

  private fun forgotPinClicks() =
      RxView.clicks(forgotPinButton).map { AppLockForgotPinClicked() }

  fun setUserFullName(fullName: String) {
    fullNameTextView.text = fullName
  }

  fun setFacilityName(facilityName: String) {
    this.facilityButton.text = facilityName
  }

  fun restorePreviousScreen() {
    screenRouter.pop()
  }

  fun exitApp() {
    activity.finish()
  }

  fun setIncorrectPinErrorVisible(show: Boolean) {
    when (show) {
      true -> errorTextView.visibility = View.VISIBLE
      else -> errorTextView.visibility = View.GONE
    }
  }

  fun setProgressVisible(show: Boolean) {
    if (show) {
      hideKeyboard()
    }

    TransitionManager.beginDelayedTransition(this, Fade()
        .setDuration(100)
        .setInterpolator(FastOutSlowInInterpolator()))

    progressView.visibility = when (show) {
      true -> View.VISIBLE
      else -> View.GONE
    }
    pinFormLayout.visibility = when (show) {
      true -> View.INVISIBLE
      else -> View.VISIBLE
    }
  }

  fun openFacilityChangeScreen() {
    screenRouter.push(FacilityChangeScreen.KEY)
  }

  fun showConfirmResetPinDialog() {
    ConfirmResetPinDialog.show(activity.supportFragmentManager)
  }
}
