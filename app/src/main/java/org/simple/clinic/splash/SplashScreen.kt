package org.simple.clinic.splash

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieDrawable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.databinding.ScreenSplashBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.onboarding.OnboardingScreen
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
class SplashScreen : Fragment() {

  @Inject
  lateinit var router: Router

  private var binding: ScreenSplashBinding? = null

  private val splashLottieView
    get() = binding!!.splashLottieView

  private val nextButton
    get() = binding!!.nextButton

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    binding = ScreenSplashBinding.inflate(inflater, container, false)
    return binding?.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    with(splashLottieView) {
      setAnimation("splash_animation.json")
      repeatCount = LottieDrawable.INFINITE
      playAnimation()
    }

    nextButton.setOnClickListener {
      router.clearHistoryAndPush(OnboardingScreen.Key())
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    binding = null
  }

  interface Injector {
    fun inject(target: SplashScreen)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Splash Screen"
  ) : ScreenKey() {

    override fun instantiateFragment() = SplashScreen()
  }
}
