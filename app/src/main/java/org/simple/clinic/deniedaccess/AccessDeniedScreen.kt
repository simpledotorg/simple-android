package org.simple.clinic.deniedaccess

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.main.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.login.applock.AppLockBackClicked
import org.simple.clinic.login.applock.AppLockForgotPinClicked
import org.simple.clinic.login.applock.AppLockPinAuthenticated
import org.simple.clinic.login.applock.AppLockScreenCreated
import org.simple.clinic.login.applock.ConfirmResetPinDialog
import org.simple.clinic.router.screen.BackPressInterceptCallback
import org.simple.clinic.router.screen.BackPressInterceptor
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.security.pin.PinAuthenticated
import org.simple.clinic.security.pin.PinDigestToVerify
import org.simple.clinic.security.pin.PinEntryCardView
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.showKeyboard
import javax.inject.Inject

class AccessDeniedScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: AppCompatActivity

  private val fullNameTextView by bindView<TextView>(R.id.applock_user_fullname)
  private val logoutButton by bindView<Button>(R.id.applock_logout)
  private val pinEntryCardView by bindView<PinEntryCardView>(R.id.applock_pin_entry_card)

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    logoutButton.setOnClickListener {
      Toast.makeText(context, "Work in progress", Toast.LENGTH_SHORT).show()
    }

    // The keyboard shows up on PIN field automatically when the app is
    // starting, but not when the user comes back from FacilityChangeScreen.
    pinEntryCardView.pinEditText.showKeyboard()
  }

  private fun screenCreates() = Observable.just(AppLockScreenCreated())

  private fun backClicks(): Observable<AppLockBackClicked> {
    return Observable.create { emitter ->
      val interceptor = object : BackPressInterceptor {
        override fun onInterceptBackPress(callback: BackPressInterceptCallback) {
          emitter.onNext(AppLockBackClicked())
          callback.markBackPressIntercepted()
        }
      }
      emitter.setCancellable { screenRouter.unregisterBackPressInterceptor(interceptor) }
      screenRouter.registerBackPressInterceptor(interceptor)
    }
  }

  fun setUserFullName(fullName: String) {
    fullNameTextView.text = fullName
  }

  fun restorePreviousScreen() {
    screenRouter.pop()
  }

  fun exitApp() {
    activity.finish()
  }
}
