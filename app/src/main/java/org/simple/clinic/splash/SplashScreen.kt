package org.simple.clinic.splash

import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieDrawable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.databinding.ScreenSplashBinding
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.compat.FullScreenKey
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.onboarding.OnboardingScreen.OnboardingScreenKey
import javax.inject.Inject

class SplashScreen : Fragment() {

  @Inject
  lateinit var router: Router

  private var binding: ScreenSplashBinding? = null

  private val splashLottieView
    get() = binding!!.splashLottieView

  private val nextButton
    get() = binding!!.nextButton

  override fun onFinishInflate() {
    super.onFinishInflate()
    binding = ScreenSplashBinding.bind(this)
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)

    with(splashLottieView) {
      setAnimation("splash_animation.json")
      repeatCount = LottieDrawable.INFINITE
      playAnimation()
    }

    nextButton.setOnClickListener {
      router.clearHistoryAndPush(OnboardingScreenKey.wrap())
    }
  }

  override fun onDetachedFromWindow() {
    binding = null
    super.onDetachedFromWindow()
  }

  interface Injector {
    fun inject(target: SplashScreen)
  }

  @Parcelize
  object SplashScreenKey : FullScreenKey {

    override val analyticsName = "Splash Screen"

    override fun layoutRes() = R.layout.screen_splash
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Splash Screen"
  ) : ScreenKey() {

    override fun instantiateFragment() = SplashScreen()
  }
}
