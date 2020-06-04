package org.simple.clinic.splash

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import com.airbnb.lottie.LottieDrawable
import kotlinx.android.synthetic.main.screen_splash.view.*
import org.simple.clinic.R

class SplashScreen(context: Context, attributeSet: AttributeSet) : ConstraintLayout(context, attributeSet) {

  override fun onFinishInflate() {
    super.onFinishInflate()
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
}
