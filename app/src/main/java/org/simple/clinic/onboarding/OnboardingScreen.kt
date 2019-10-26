package org.simple.clinic.onboarding

import android.content.Context
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.f2prateek.rx.preferences2.Preference
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.android.synthetic.main.screen_onboarding.view.*
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.injector
import org.simple.clinic.main.TheActivity
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.util.clamp
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject
import javax.inject.Named

class OnboardingScreen(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet), OnboardingUi {

  @Inject
  lateinit var activity: AppCompatActivity

  @field:[Inject Named("onboarding_complete")]
  lateinit var hasUserCompletedOnboarding: Preference<Boolean>

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  @Inject
  lateinit var crashReporter: CrashReporter

  private val events: Observable<OnboardingEvent>
    get() = getStartedClicks()
        .compose(ReportAnalyticsEvents())
        .cast()

  private val delegate by unsafeLazy {
    MobiusDelegate(
        events,
        OnboardingModel,
        null,
        OnboardingUpdate(),
        OnboardingEffectHandler.createEffectHandler(hasUserCompletedOnboarding, this, schedulersProvider),
        { /* No-op, there's nothing to render */ },
        crashReporter
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }

    context.injector<OnboardingScreenInjector>().inject(this)

    fadeLogoWithContentScroll()
    delegate.prepare()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  private fun fadeLogoWithContentScroll() {
    scrollView.setOnScrollChangeListener { _: NestedScrollView, _: Int, scrollY: Int, _: Int, _: Int ->
      val distanceBetweenLogoAndScrollTop = appLogoImageView.top - scrollY.toFloat()
      val opacity = (distanceBetweenLogoAndScrollTop / appLogoImageView.top).clamp(0F, 1F)
      appLogoImageView.alpha = opacity
    }
  }

  private fun getStartedClicks(): Observable<OnboardingEvent> {
    return RxView.clicks(getStartedButton).map { GetStartedClicked }
  }

  override fun moveToRegistrationScreen() {
    // This navigation should not be done here, we need a way to publish
    // an event to the parent activity (maybe via the screen router's
    // event bus?) and handle the navigation there.
    // TODO(vs): 2019-10-26 Move this to an event that is subscribed in the parent activity
    val intent = TheActivity.newIntent(activity).apply {
      flags = FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_NO_ANIMATION
    }
    activity.startActivity(intent)
    activity.overridePendingTransition(0, 0)
  }
}
