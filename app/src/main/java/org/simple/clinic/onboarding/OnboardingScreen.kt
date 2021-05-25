package org.simple.clinic.onboarding

import android.content.Context
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenOnboardingBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.registerorlogin.AuthenticationActivity
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.router.util.resolveColor
import org.simple.clinic.util.disableAnimations
import org.simple.clinic.util.finishWithoutAnimations
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class OnboardingScreen(
    context: Context,
    attributeSet: AttributeSet
) : ConstraintLayout(context, attributeSet), OnboardingUi {

  @Inject
  lateinit var onboardingEffectHandler: OnboardingEffectHandler.Factory

  @Inject
  lateinit var activity: AppCompatActivity

  private var binding: ScreenOnboardingBinding? = null

  private val getStartedButton
    get() = binding!!.getStartedButton

  private val introOneTextView
    get() = binding!!.introOneTextView

  private val introTwoTextView
    get() = binding!!.introTwoTextView

  private val introThreeTextView
    get() = binding!!.introThreeTextView

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
    binding = ScreenOnboardingBinding.bind(this)
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
    binding = null
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
    val onboardingEmphasisTextSpan = TextAppearanceSpan(context, R.style.TextAppearance_Simple_Subtitle1_Medium)
    val onboardingEmphasisColorSpan = ForegroundColorSpan(context.resolveColor(attrRes = R.attr.colorOnBackground))

    val introOneFormattedString = buildSpannedString {
      append(context.getString(R.string.screenonboarding_intro_1))

      inSpans(onboardingEmphasisTextSpan, onboardingEmphasisColorSpan) {
        append(context.getString(R.string.screenonboarding_intro_1_hypertension))
      }
    }

    introOneTextView.text = introOneFormattedString
  }

  private fun setIntroTwoTextView() {
    val introTwoFormattedString = buildSpannedString {
      append(context.getString(R.string.screenonboarding_intro_2))

      inSpans(
          TextAppearanceSpan(context, R.style.TextAppearance_Simple_Subtitle1_Medium),
          ForegroundColorSpan(context.resolveColor(attrRes = R.attr.colorOnBackground))
      ) {
        append(context.getString(R.string.screenonboarding_intro_2_bp))
      }

      append(context.getString(R.string.screenonboarding_intro_2_and))

      inSpans(
          TextAppearanceSpan(context, R.style.TextAppearance_Simple_Subtitle1_Medium),
          ForegroundColorSpan(context.resolveColor(attrRes = R.attr.colorOnBackground))
      ) {
        append(context.getString(R.string.screenonboarding_intro_2_medicines))
      }
    }

    introTwoTextView.text = introTwoFormattedString
  }

  private fun setIntroThreeTextView() {
    val onboardingEmphasisTextSpan = TextAppearanceSpan(context, R.style.TextAppearance_Simple_Subtitle1_Medium)
    val onboardingEmphasisColorSpan = ForegroundColorSpan(context.resolveColor(attrRes = R.attr.colorOnBackground))

    val introThreeFormattedString = buildSpannedString {
      append(context.getString(R.string.screenonboarding_intro_3))

      inSpans(onboardingEmphasisTextSpan, onboardingEmphasisColorSpan) {
        append(context.getString(R.string.screenonboarding_intro_3_reminder))
      }

      append(context.getString(R.string.screenonboarding_intro_3_visits))
    }

    introThreeTextView.text = introThreeFormattedString
  }

  @Parcelize
  object OnboardingScreenKey : FullScreenKey {

    override val analyticsName = "Onboarding Screen"

    override fun layoutRes() = R.layout.screen_onboarding
  }
}
