package org.simple.clinic.onboarding

import android.content.Context
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.android.synthetic.main.screen_onboarding.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.registerorlogin.AuthenticationActivity
import org.simple.clinic.util.Truss
import org.simple.clinic.util.disableAnimations
import org.simple.clinic.util.finishWithoutAnimations
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class OnboardingScreen(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet), OnboardingUi {

  @Inject
  lateinit var onboardingEffectHandler: OnboardingEffectHandler.Factory

  @Inject
  lateinit var activity: AppCompatActivity

  private val events: Observable<OnboardingEvent> by unsafeLazy {
    getStartedClicks()
        .compose(ReportAnalyticsEvents())
        .cast<OnboardingEvent>()
  }

  private val delegate by unsafeLazy {
    MobiusDelegate.forView(
        events,
        OnboardingModel,
        OnboardingUpdate(),
        onboardingEffectHandler.create(this).build()
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<OnboardingScreenInjector>().inject(this)

    setIntroOneTextView()
    setIntroTwoTextView()
    setIntroThreeTextView()
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  private fun getStartedClicks(): Observable<OnboardingEvent> {
    return getStartedButton.clicks().map { GetStartedClicked }
  }

  override fun moveToRegistrationScreen() {
    // This navigation should not be done here, we need a way to publish
    // an event to the parent activity (maybe via the screen router's
    // event bus?) and handle the navigation there.
    // TODO(vs): 2019-11-07 Move this to an event that is subscribed in the parent activity
    val intent = AuthenticationActivity
        .forNewLogin(activity)
        .disableAnimations()

    activity.startActivity(intent)
    activity.finishWithoutAnimations()
  }

  private fun setIntroOneTextView() {
    val introOneFormattedString = Truss()
        .append(context.getString(R.string.screenonboarding_intro_1))
        .pushSpan(TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Subtitle1Left_Emphasis_Grey0))
        .append(context.getString(R.string.screenonboarding_intro_1_hypertension))
        .popSpan()
        .build()

    introOneTextView.text = introOneFormattedString
  }

  private fun setIntroTwoTextView() {
    val introTwoFormattedString = Truss()
        .append(context.getString(R.string.screenonboarding_intro_2))
        .pushSpan(TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Subtitle1Left_Emphasis_Grey0))
        .append(context.getString(R.string.screenonboarding_intro_2_bp))
        .popSpan()
        .append(context.getString(R.string.screenonboarding_intro_2_and))
        .pushSpan(TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Subtitle1Left_Emphasis_Grey0))
        .append(context.getString(R.string.screenonboarding_intro_2_medicines))
        .popSpan()
        .build()

    introTwoTextView.text = introTwoFormattedString
  }

  private fun setIntroThreeTextView() {
    val introThreeFormattedString = Truss()
        .append(context.getString(R.string.screenonboarding_intro_3))
        .pushSpan(TextAppearanceSpan(context, R.style.Clinic_V2_TextAppearance_Subtitle1Left_Emphasis_Grey0))
        .append(context.getString(R.string.screenonboarding_intro_3_reminder))
        .popSpan()
        .append(context.getString(R.string.screenonboarding_intro_3_visits))
        .build()

    introThreeTextView.text = introThreeFormattedString
  }
}
