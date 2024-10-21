package org.simple.clinic.splash

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.common.ui.components.FilledButton
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.onboarding.OnboardingScreen
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
class SplashScreen : Fragment() {

  @Inject
  lateinit var router: Router

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return ComposeView(requireContext()).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

      setContent {
        SimpleTheme {
          SplashScreen(
              onNextClick = {
                router.clearHistoryAndPush(OnboardingScreen.Key())
              }
          )
        }
      }
    }
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

@Composable
private fun SplashScreen(
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Scaffold(
      modifier = Modifier
          .fillMaxWidth()
          .then(modifier),
      bottomBar = {
        Box(
            modifier = Modifier
                .background(SimpleTheme.colors.material.primaryVariant)
                .padding(12.dp)
        ) {
          FilledButton(
              modifier = Modifier.fillMaxWidth(),
              onClick = onNextClick,
          ) {
            Text(
                text = stringResource(R.string.screensplash_next).uppercase()
            )
          }
        }
      },
      backgroundColor = SimpleTheme.colors.toolbarPrimary,
  ) { innerPadding ->
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(innerPadding)
    ) {
      Image(
          modifier = Modifier
              .fillMaxWidth()
              .padding(top = 96.dp)
              .requiredSizeIn(minHeight = 40.dp),
          painter = painterResource(R.drawable.logo_large),
          contentDescription = null,
      )

      val composition by rememberLottieComposition(LottieCompositionSpec.Asset("splash_animation.json"))
      LottieAnimation(
          composition = composition,
          alignment = Alignment.BottomCenter,
          iterations = LottieConstants.IterateForever,
      )
    }
  }
}

@Preview
@Composable
private fun SplashScreenPreview() {
  SimpleTheme {
    SplashScreen(
        onNextClick = {
          // no-op
        }
    )
  }
}
