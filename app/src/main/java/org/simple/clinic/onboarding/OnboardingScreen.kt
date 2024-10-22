package org.simple.clinic.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.common.ui.components.FilledButtonWithFrame
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.consent.onboarding.OnboardingConsentScreenFragment
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.DisposableViewEffect
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class OnboardingScreen : Fragment(), OnboardingUi {

  @Inject
  lateinit var effectHandlerFactory: OnboardingEffectHandler.Factory

  @Inject
  lateinit var router: Router

  private val viewEffectHandler by unsafeLazy { OnboardingViewEffectHandler(this) }
  private val viewModel by viewModels<OnboardingViewModel>(
      factoryProducer = { OnboardingViewModel.factory(effectHandlerFactory) }
  )

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return ComposeView(requireContext()).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

      setContent {
        SimpleTheme {
          viewModel.viewEffects.DisposableViewEffect(viewEffectHandler::handle)

          OnboardingScreen(
              onGetStartedClick = {
                viewModel.dispatch(event = GetStartedClicked)
              }
          )
        }
      }
    }
  }

  override fun openOnboardingConsentScreen() {
    router.clearHistoryAndPush(OnboardingConsentScreenFragment.Key())
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

@Composable
private fun OnboardingScreen(
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Scaffold(
      modifier = Modifier
          .fillMaxSize()
          .then(modifier),
      bottomBar = {
        FilledButtonWithFrame(
            onClick = onGetStartedClick,
        ) {
          Text(
              text = stringResource(R.string.onboarding_get_started).uppercase()
          )
        }
      },
      backgroundColor = SimpleTheme.colors.material.surface,
  ) { innerPadding ->
    val emphasisSpanStyle = SpanStyle(
        color = SimpleTheme.colors.material.onBackground,
        fontSize = SimpleTheme.typography.subtitle1Medium.fontSize,
        fontStyle = SimpleTheme.typography.subtitle1Medium.fontStyle,
        fontFamily = SimpleTheme.typography.subtitle1Medium.fontFamily,
        fontWeight = SimpleTheme.typography.subtitle1Medium.fontWeight,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 32.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.SpaceAround,
    ) {
      OnboardingInfo1(emphasisSpanStyle)
      OnboardingInfo2(emphasisSpanStyle)
      OnboardingInfo3(emphasisSpanStyle)
    }
  }
}

@Composable
private fun OnboardingInfo1(
    spanStyle: SpanStyle,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier = Modifier
          .fillMaxWidth()
          .then(modifier),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(24.dp)
  ) {
    Image(
        painter = painterResource(R.drawable.ic_onboarding_intro_1),
        contentDescription = null
    )

    Text(
        text = buildAnnotatedString {
          append(stringResource(R.string.screenonboarding_intro_1))

          withStyle(spanStyle) {
            append(stringResource(R.string.screenonboarding_intro_1_hypertension))
          }
        },
        style = MaterialTheme.typography.subtitle1,
        color = SimpleTheme.colors.material.onSurface.copy(alpha = 0.67f)
    )
  }
}

@Composable
private fun OnboardingInfo2(
    spanStyle: SpanStyle,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier = Modifier
          .fillMaxWidth()
          .then(modifier),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(24.dp)
  ) {
    Image(
        painter = painterResource(R.drawable.ic_onboarding_intro_2),
        contentDescription = null
    )

    Text(
        text = buildAnnotatedString {
          append(stringResource(R.string.screenonboarding_intro_2))

          withStyle(spanStyle) {
            append(stringResource(R.string.screenonboarding_intro_2_bp))
          }

          append(stringResource(R.string.screenonboarding_intro_2_and))

          withStyle(spanStyle) {
            append(stringResource(R.string.screenonboarding_intro_2_medicines))
          }
        },
        style = MaterialTheme.typography.subtitle1,
        color = SimpleTheme.colors.material.onSurface.copy(alpha = 0.67f)
    )
  }
}

@Composable
private fun OnboardingInfo3(
    spanStyle: SpanStyle,
    modifier: Modifier = Modifier,
) {
  Row(
      modifier = Modifier
          .fillMaxWidth()
          .then(modifier),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(24.dp)
  ) {
    Image(
        painter = painterResource(R.drawable.ic_onboarding_intro_3),
        contentDescription = null
    )

    Text(
        text = buildAnnotatedString {
          append(stringResource(R.string.screenonboarding_intro_3))

          withStyle(spanStyle) {
            append(stringResource(R.string.screenonboarding_intro_3_reminder))
          }

          append(stringResource(R.string.screenonboarding_intro_3_visits))
        },
        style = SimpleTheme.typography.material.subtitle1,
        color = SimpleTheme.colors.material.onSurface.copy(alpha = 0.67f)
    )
  }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
  SimpleTheme {
    OnboardingScreen()
  }
}
