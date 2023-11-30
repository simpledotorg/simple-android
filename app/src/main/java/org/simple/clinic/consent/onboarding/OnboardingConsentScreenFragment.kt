package org.simple.clinic.consent.onboarding

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.common.ui.components.FilledButton
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.consent.onboarding.OnboardingConsentEvent.AgreeButtonClicked
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.registerorlogin.AuthenticationActivity
import org.simple.clinic.util.disableAnimations
import org.simple.clinic.util.finishWithoutAnimations
import org.simple.clinic.util.mobiusViewModels
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class OnboardingConsentScreenFragment : Fragment(), UiActions {

  @Inject
  lateinit var effectHandlerFactory: OnboardingConsentEffectHandler.Factory

  private val viewEffectHandler by unsafeLazy { OnboardingConsentViewEffectHandler(this) }
  private val viewModel by mobiusViewModels(
      defaultModel = { OnboardingConsentModel },
      update = { OnboardingConsentUpdate() },
      effectHandler = { viewEffectsConsumer -> effectHandlerFactory.create(viewEffectsConsumer).build() }
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
          OnboardingConsentScreen(
              onAccept = { viewModel.dispatchEvent(AgreeButtonClicked) }
          )
        }
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.viewEffects.setObserver(
        viewLifecycleOwner,
        { viewEffect -> viewEffectHandler.handle(viewEffect) },
        { pausedViewEffects -> pausedViewEffects.forEach { viewEffectHandler.handle(it) } }
    )
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
    fun inject(target: OnboardingConsentScreenFragment)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Onboarding Consent Screen"
  ) : ScreenKey() {

    override fun instantiateFragment() = OnboardingConsentScreenFragment()
  }
}

@Composable
private fun OnboardingConsentScreen(
    modifier: Modifier = Modifier,
    onAccept: () -> Unit
) {
  Scaffold(
      modifier = modifier,
      bottomBar = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SimpleTheme.colors.material.primaryVariant)
                .padding(dimensionResource(id = R.dimen.spacing_8))
        ) {
          FilledButton(
              onClick = onAccept,
              modifier = Modifier.fillMaxWidth()
          ) {
            Text(text = stringResource(id = R.string.screen_onboarding_concent_accept_button))
          }
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

  Card(
      modifier = modifier
          .fillMaxWidth()
          .padding(horizontal = spacing24),
  ) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(spacing24),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
          text = stringResource(id = R.string.screen_onboarding_consent_title),
          style = SimpleTheme.typography.material.h6
      )

      Spacer(modifier = Modifier.requiredHeight(spacing24))

      Text(
          text = stringResource(id = R.string.screen_onboarding_consent_subtitle),
          style = SimpleTheme.typography.material.body1
      )
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
