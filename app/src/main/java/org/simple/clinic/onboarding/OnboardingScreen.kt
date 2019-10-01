package org.simple.clinic.onboarding

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.core.widget.NestedScrollView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_onboarding.view.*
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.registration.phone.RegistrationPhoneScreenKey
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.clamp
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class OnboardingScreen(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet), OnboardingUi {

  @Inject
  lateinit var router: ScreenRouter

  @Inject
  lateinit var controller: OnboardingScreenController

  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    fadeLogoWithContentScroll()

    bindUiToController(
        ui = this,
        events = getStartedClicks(),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
  }

  private fun fadeLogoWithContentScroll() {
    scrollView.setOnScrollChangeListener { _: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
      val distanceBetweenLogoAndScrollTop = appLogoImageView.top - scrollY.toFloat()
      val opacity = (distanceBetweenLogoAndScrollTop / appLogoImageView.top).clamp(0F, 1F)
      appLogoImageView.alpha = opacity
    }
  }

  private fun getStartedClicks(): Observable<UiEvent> {
    return RxView.clicks(getStartedButton).map { GetStartedClicked }
  }

  override fun moveToRegistrationScreen() {
    router.clearHistoryAndPush(RegistrationPhoneScreenKey(), RouterDirection.FORWARD)
  }
}
