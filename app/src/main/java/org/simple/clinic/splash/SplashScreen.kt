package org.simple.clinic.splash

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import com.airbnb.lottie.LottieDrawable
import org.simple.clinic.R
import org.simple.clinic.databinding.ScreenSplashBinding

class SplashScreen(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet) {

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

    with(splashLottieView) {
      setAnimation("splash_animation.json")
      repeatCount = LottieDrawable.INFINITE
      playAnimation()
    }

    nextButton.setOnClickListener {
      findNavController().navigate(R.id.action_splashScreen_to_onboardingScreen)
    }
  }

  override fun onDetachedFromWindow() {
    binding = null
    super.onDetachedFromWindow()
  }
}
