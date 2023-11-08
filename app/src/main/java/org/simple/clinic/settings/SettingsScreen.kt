package org.simple.clinic.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.parcelize.Parcelize
import org.simple.clinic.PLAY_STORE_URL_FOR_SIMPLE
import org.simple.clinic.R
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.HandlesBack
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.settings.changelanguage.ChangeLanguageScreen
import org.simple.clinic.setup.SetupActivity
import org.simple.clinic.util.mobiusViewModels
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class SettingsScreen : Fragment(), UiActions, HandlesBack {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var settingsEffectHandler: SettingsEffectHandler.Factory

  @Inject
  lateinit var features: Features

  private val isChangeLanguageFeatureEnabled by unsafeLazy {
    features.isEnabled(Feature.ChangeLanguage)
  }
  private val isLogoutUserFeatureEnabled by unsafeLazy {
    features.isEnabled(Feature.LogoutUser)
  }

  private val viewEffectHandler by unsafeLazy { SettingsViewEffectHandler(this) }
  private val viewModel by mobiusViewModels(
      defaultModel = {
        SettingsModel.default(
            isChangeLanguageFeatureEnabled = isChangeLanguageFeatureEnabled,
            isLogoutUserFeatureEnabled = isLogoutUserFeatureEnabled
        )
      },
      init = { SettingsInit() },
      update = { SettingsUpdate() },
      effectHandler = { viewEffectsConsumer ->
        settingsEffectHandler.create(viewEffectsConsumer).build()
      }
  )

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return ComposeView(requireContext()).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        val settingsModel by viewModel.models.observeAsState(
            initial = SettingsModel.default(
                isChangeLanguageFeatureEnabled = isChangeLanguageFeatureEnabled,
                isLogoutUserFeatureEnabled = isLogoutUserFeatureEnabled
            )
        )
        settingsModel?.let {
          SettingsScreen(
              model = it,
              navigationIconClick = { onBackPressed() },
              changeLanguageButtonClick = { viewModel.dispatchEvent(ChangeLanguage) },
              updateButtonClick = { launchPlayStoreForUpdate() },
              logoutButtonClick = { viewModel.dispatchEvent(LogoutButtonClicked) }
          )
        }
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    viewModel.viewEffects.setObserver(
        viewLifecycleOwner,
        { liveViewEffect -> viewEffectHandler.handle(liveViewEffect) },
        { pausedViewEffects -> pausedViewEffects.forEach { viewEffectHandler.handle(it) } }
    )
  }

  override fun openLanguageSelectionScreen() {
    router.push(ChangeLanguageScreen.Key())
  }

  override fun showConfirmLogoutDialog() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.settings_logout_dialog_title)
        .setMessage(R.string.settings_logout_dialog_desc)
        .setPositiveButton(R.string.settings_logout_dialog_positive_action) { _, _ ->
          viewModel.dispatchEvent(ConfirmLogoutButtonClicked)
        }
        .setNegativeButton(R.string.settings_logout_dialog_negative_action, null)
        .show()
  }

  override fun restartApp() {
    val intent = Intent(requireContext(), SetupActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(intent)
  }

  override fun goBack() {
    router.pop()
  }

  override fun onBackPressed(): Boolean {
    viewModel.dispatchEvent(BackClicked)
    return true
  }

  private fun launchPlayStoreForUpdate() {
    val intent = Intent(Intent.ACTION_VIEW).apply {
      data = Uri.parse(PLAY_STORE_URL_FOR_SIMPLE)
    }
    requireContext().startActivity(intent)
  }

  interface Injector {
    fun inject(target: SettingsScreen)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Settings"
  ) : ScreenKey() {
    override fun instantiateFragment(): Fragment = SettingsScreen()
  }
}
