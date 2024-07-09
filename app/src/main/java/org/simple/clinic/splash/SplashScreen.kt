package org.simple.clinic.splash

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import org.simple.clinic.databinding.ScreenSplashBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.onboarding.OnboardingScreen

@SuppressLint("CustomSplashScreen")
class SplashScreen : Fragment() {

  private var binding: ScreenSplashBinding? = null

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
    nextButton.setOnClickListener {
      findNavController().navigate(OnboardingScreen.Key()) {
        popUpTo<Key> { inclusive = true }
      }
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
  @Serializable
  data class Key(
      override val analyticsName: String = "Splash Screen"
  ) : ScreenKey() {

    override fun instantiateFragment() = SplashScreen()
  }
}
