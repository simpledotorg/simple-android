package org.simple.clinic.onboarding

import android.content.Context
import android.support.v4.widget.NestedScrollView
import android.util.AttributeSet
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
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

  private val appLogo by bindView<TextView>(R.id.onboarding_logo)
  private val scrollingContainer by bindView<NestedScrollView>(R.id.onboarding_scrolling_container)
  private val getStartedButton by bindView<Button>(R.id.onboarding_get_started)

  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) return

    TheActivity.component.inject(this)

    fadeLogoWithContainerScroll()
  }

  private fun fadeLogoWithContainerScroll() {
    scrollingContainer.setOnScrollChangeListener { _: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
      val distanceBetweenLogoAndScrollTop = appLogo.top - scrollY.toFloat()
      val opacity = (distanceBetweenLogoAndScrollTop / appLogo.top).clamp(0F, 1F)
      appLogo.alpha = opacity
    }
  }
}