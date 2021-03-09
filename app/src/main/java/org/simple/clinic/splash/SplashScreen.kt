package org.simple.clinic.splash

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.lottie.LottieDrawable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.databinding.ScreenSplashBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.onboarding.OnboardingScreen.OnboardingScreenKey
import org.simple.clinic.router.screen.FullScreenKey
import javax.inject.Inject

class SplashScreen(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet) {

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
}
