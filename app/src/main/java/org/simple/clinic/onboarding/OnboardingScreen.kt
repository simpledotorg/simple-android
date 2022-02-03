package org.simple.clinic.onboarding

import android.content.Context
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenOnboardingBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.registerorlogin.AuthenticationActivity
import org.simple.clinic.util.disableAnimations
import org.simple.clinic.util.finishWithoutAnimations
import org.simple.clinic.util.resolveColor
import javax.inject.Inject

class OnboardingScreen : BaseScreen<
    OnboardingScreen.Key,
    ScreenOnboardingBinding,
    OnboardingModel,
    OnboardingEvent,
    OnboardingEffect,
    OnboardingViewEffect>(), OnboardingUi {

  @Inject
  lateinit var onboardingEffectHandler: OnboardingEffectHandler.Factory

  @Inject
  lateinit var activity: AppCompatActivity

  private val getStartedButton
    get() = binding.getStartedButton

  private val introOneTextView
    get() = binding.introOneTextView

  private val introTwoTextView
    get() = binding.introTwoTextView

  private val introThreeTextView
    get() = binding.introThreeTextView

  override fun defaultModel() = OnboardingModel

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenOnboardingBinding.inflate(layoutInflater, container, false)

  override fun createUpdate() = OnboardingUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<OnboardingViewEffect>) = onboardingEffectHandler
      .create(
          viewEffectsConsumer = viewEffectsConsumer
      )
      .build()

  override fun events() = getStartedClicks()
      .compose(ReportAnalyticsEvents())
      .cast<OnboardingEvent>()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setIntroOneTextView()
    setIntroTwoTextView()
    setIntroThreeTextView()
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
    val onboardingEmphasisColorSpan = ForegroundColorSpan(requireContext().resolveColor(attrRes = R.attr.colorOnBackground))

    val introOneFormattedString = buildSpannedString {
      append(requireContext().getString(R.string.screenonboarding_intro_1))

      inSpans(onboardingEmphasisTextSpan, onboardingEmphasisColorSpan) {
        append(requireContext().getString(R.string.screenonboarding_intro_1_hypertension))
      }
    }

    introOneTextView.text = introOneFormattedString
  }

  private fun setIntroTwoTextView() {
    val introTwoFormattedString = buildSpannedString {
      append(requireContext().getString(R.string.screenonboarding_intro_2))

      inSpans(
          TextAppearanceSpan(context, R.style.TextAppearance_Simple_Subtitle1_Medium),
          ForegroundColorSpan(requireContext().resolveColor(attrRes = R.attr.colorOnBackground))
      ) {
        append(requireContext().getString(R.string.screenonboarding_intro_2_bp))
      }

      append(requireContext().getString(R.string.screenonboarding_intro_2_and))

      inSpans(
          TextAppearanceSpan(context, R.style.TextAppearance_Simple_Subtitle1_Medium),
          ForegroundColorSpan(requireContext().resolveColor(attrRes = R.attr.colorOnBackground))
      ) {
        append(requireContext().getString(R.string.screenonboarding_intro_2_medicines))
      }
    }

    introTwoTextView.text = introTwoFormattedString
  }

  private fun setIntroThreeTextView() {
    val onboardingEmphasisTextSpan = TextAppearanceSpan(context, R.style.TextAppearance_Simple_Subtitle1_Medium)
    val onboardingEmphasisColorSpan = ForegroundColorSpan(requireContext().resolveColor(attrRes = R.attr.colorOnBackground))

    val introThreeFormattedString = buildSpannedString {
      append(requireContext().getString(R.string.screenonboarding_intro_3))

      inSpans(onboardingEmphasisTextSpan, onboardingEmphasisColorSpan) {
        append(requireContext().getString(R.string.screenonboarding_intro_3_reminder))
      }

      append(requireContext().getString(R.string.screenonboarding_intro_3_visits))
    }

    introThreeTextView.text = introThreeFormattedString
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Onboarding Screen"
  ) : ScreenKey() {
    override fun instantiateFragment() = OnboardingScreen()
  }

  interface Injector {
    fun inject(target: OnboardingScreen)
  }
}
