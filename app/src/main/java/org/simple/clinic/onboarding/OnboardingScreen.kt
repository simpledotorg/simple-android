package org.simple.clinic.onboarding

import android.content.Context
import android.support.v4.widget.NestedScrollView
import android.util.AttributeSet
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.registration.phone.RegistrationPhoneScreen
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.clamp
import javax.inject.Inject

class OnboardingScreen(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet) {

  companion object {
    @JvmField
    val KEY = OnboardingScreenKey()
  }

  @Inject
  lateinit var router: ScreenRouter

  @Inject
  lateinit var controller: OnboardingScreenController

  private val appLogoImageView by bindView<ImageView>(R.id.onboarding_logo)
  private val scrollView by bindView<NestedScrollView>(R.id.onboarding_scrolling_container)
  private val getStartedButton by bindView<Button>(R.id.onboarding_get_started)

  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    fadeLogoWithContentScroll()

    getStartedClicks()
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun fadeLogoWithContentScroll() {
    scrollView.setOnScrollChangeListener { _: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
      val distanceBetweenLogoAndScrollTop = appLogoImageView.top - scrollY.toFloat()
      val opacity = (distanceBetweenLogoAndScrollTop / appLogoImageView.top).clamp(0F, 1F)
      appLogoImageView.alpha = opacity
    }
  }

  private fun getStartedClicks() =
      RxView.clicks(getStartedButton).map { OnboardingGetStartedClicked() }

  fun moveToRegistrationScreen() {
    router.clearHistoryAndPush(RegistrationPhoneScreen.KEY, RouterDirection.FORWARD)
  }
}
