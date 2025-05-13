package org.simple.clinic.consent.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.common.ui.components.FilledButtonWithFrame
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.consent.onboarding.OnboardingConsentEvent.AgreeButtonClicked
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.DisposableViewEffect
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.registerorlogin.AuthenticationActivity
import org.simple.clinic.util.disableAnimations
import org.simple.clinic.util.finishWithoutAnimations
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class OnboardingConsentScreen : Fragment(), UiActions {

  @Inject
  lateinit var effectHandlerFactory: OnboardingConsentEffectHandler.Factory

  private val viewEffectHandler by unsafeLazy { OnboardingConsentViewEffectHandler(this) }
  private val viewModel by viewModels<OnboardingConsentViewModel>(
      factoryProducer = { OnboardingConsentViewModel.factory(effectHandlerFactory) }
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

          OnboardingConsentScreen(
              onAccept = { viewModel.dispatch(event = AgreeButtonClicked) }
          )
        }
      }
    }
  }

  override fun moveToRegistrationActivity() {
    // This navigation should not be done here, we need a way to publish
    // an event to the parent activity (maybe via the screen router's
    // event bus?) and handle the navigation there.
    // TODO(vs): 2019-11-07 Move this to an event that is subscribed in the parent activity
    val intent = AuthenticationActivity
        .forNewLogin(requireActivity())
        .disableAnimations()

    activity?.startActivity(intent)
    activity?.finishWithoutAnimations()
  }

  interface Injector {
    fun inject(target: OnboardingConsentScreen)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Onboarding Consent Screen"
  ) : ScreenKey() {

    override fun instantiateFragment() = OnboardingConsentScreen()
  }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun OnboardingConsentScreen(
    modifier: Modifier = Modifier,
    onAccept: () -> Unit
) {
  Scaffold(
      modifier = modifier,
      bottomBar = {
        FilledButtonWithFrame(
            testTag = "agreeAndContinue",
            onClick = onAccept
        ) {
          Text(text = stringResource(id = R.string.data_protection_consent_accept_button))
        }
      }
  ) { paddingValues ->
    val toolbarColor = SimpleTheme.colors.toolbarPrimary
    val toolbarHeight = dimensionResource(id = R.dimen.spacing_192)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .drawWithCache {
              onDrawWithContent {
                drawRect(
                    color = toolbarColor,
                    size = size.copy(height = toolbarHeight.toPx())
                )
                drawContent()
              }
            }
            .statusBarsPadding()
    ) {
      Image(
          modifier = Modifier
              .padding(
                  top = dimensionResource(id = R.dimen.spacing_40),
                  bottom = dimensionResource(id = R.dimen.spacing_44)
              )
              .align(Alignment.CenterHorizontally),
          painter = painterResource(id = R.drawable.logo_large),
          contentDescription = null
      )

      ConsentCard()
    }
  }
}

@Composable
private fun ConsentCard(modifier: Modifier = Modifier) {
  val spacing24 = dimensionResource(id = R.dimen.spacing_24)

  Column(
      modifier = Modifier
          .verticalScroll(rememberScrollState())
          .clipScrollableContainer(Orientation.Vertical)
  ) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = spacing24)
            .padding(bottom = dimensionResource(id = R.dimen.spacing_16)),
    ) {
      Column(
          modifier = Modifier.padding(spacing24),
          horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
            text = stringResource(id = R.string.data_protection_consent_title),
            style = SimpleTheme.typography.material.h6
        )

        Spacer(modifier = Modifier.requiredHeight(spacing24))

        Text(
            text = stringResource(id = R.string.data_protection_consent_subtitle),
            style = SimpleTheme.typography.material.body1
        )
      }
    }
  }
}

@Preview
@Composable
private fun OnboardingConsentScreenPreview() {
  SimpleTheme {
    OnboardingConsentScreen(
        onAccept = {
          // no-op
        }
    )
  }
}
