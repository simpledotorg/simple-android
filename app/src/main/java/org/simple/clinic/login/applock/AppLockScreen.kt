package org.simple.clinic.login.applock

import android.content.Context
import android.support.transition.Fade
import android.support.transition.TransitionManager
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.router.screen.BackPressInterceptCallback
import org.simple.clinic.router.screen.BackPressInterceptor
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.hideKeyboard
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

  private val rootLayout by bindView<ViewGroup>(R.id.applock_root)
  private val progressView by bindView<ProgressBar>(R.id.applock_progress)
  private val fullNameTextView by bindView<TextView>(R.id.applock_user_fullname)
  private val pinEditText by bindView<EditText>(R.id.applock_pin)
  private val pinFormLayout by bindView<LinearLayout>(R.id.applock_pin_container)
  private val errorTextView by bindView<TextView>(R.id.applock_error)
  private val userLogoutTextView by bindView<TextView>(R.id.applock_user_logouut)
  private val forgotPinButton by bindView<Button>(R.id.applock_forgotpin)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable.mergeArray(screenCreates(), pinTextChanges(), submitClicks(), backClicks(), logoutClicks(), forgotPinClicks())
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates() = Observable.just(AppLockScreenCreated())

  private fun pinTextChanges() =
      RxTextView.textChanges(pinEditText)
          .map(CharSequence::toString)
          .map(::AppLockScreenPinTextChanged)

  private fun submitClicks() =
      RxTextView.textChanges(pinEditText)
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

  private fun logoutClicks() =
      RxView.clicks(userLogoutTextView).map { LogoutClicked() }

  private fun forgotPinClicks() =
      RxView.clicks(forgotPinButton).map { ForgotPinClicked() }

  fun logoutDone() {
    Toast.makeText(context, R.string.not_yet_implemented, Toast.LENGTH_SHORT).show()
  }

  fun showCurrentPinResetRequestStatus() {
    Toast.makeText(context, R.string.not_yet_implemented, Toast.LENGTH_SHORT).show()
  }

  fun showFullName(fullName: String) {
    fullNameTextView.text = fullName
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
      rootLayout.hideKeyboard()
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
}
