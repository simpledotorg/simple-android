package org.simple.clinic.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.PLAY_STORE_URL_FOR_SIMPLE
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenSettingsBinding
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.settings.changelanguage.ChangeLanguageScreen
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import javax.inject.Inject

class SettingsScreen : BaseScreen<
    SettingsScreen.Key,
    ScreenSettingsBinding,
    SettingsModel,
    SettingsEvent,
    SettingsEffect,
    SettingsViewEffect>(), SettingsUi, UiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var settingsEffectHandler: SettingsEffectHandler.Factory

  @Inject
  lateinit var features: Features

  private val changeLanguageButton
    get() = binding.changeLanguageButton

  private val toolbar
    get() = binding.toolbar

  private val updateAppVersionButton
    get() = binding.updateAppVersionButton

  private val userName
    get() = binding.userName

  private val userNumber
    get() = binding.userNumber

  private val currentLanguage
    get() = binding.currentLanguage

  private val appVersion
    get() = binding.appVersion

  private val changeLanguageWidgetGroup
    get() = binding.changeLanguageWidgetGroup

  private val logoutButton
    get() = binding.logoutButton

  private val isChangeLanguageFeatureEnabled by unsafeLazy { features.isEnabled(Feature.ChangeLanguage) }
  private val isLogoutUserFeatureEnabled by unsafeLazy {
    features.isEnabled(Feature.LogoutUser)
  }
  private val hotEvents = PublishSubject.create<UiEvent>()

  override fun defaultModel() = SettingsModel.default()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenSettingsBinding.inflate(layoutInflater, container, false)

  override fun createInit() = SettingsInit()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<SettingsViewEffect>) =
      settingsEffectHandler
          .create(
              viewEffectsConsumer = viewEffectsConsumer
          )
          .build()

  override fun createUpdate() = SettingsUpdate()

  override fun events() = Observable
      .mergeArray(
          changeLanguageButtonClicks(),
          logoutButtonClicks(),
          hotEvents
      )
      .compose(ReportAnalyticsEvents())
      .cast<SettingsEvent>()

  override fun uiRenderer() = SettingsUiRenderer(this)

  override fun viewEffectHandler() = SettingsViewEffectHandler(this)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toggleChangeLanguageFeature()
    toolbar.setNavigationOnClickListener { router.pop() }

    updateAppVersionButton.setOnClickListener {
      launchPlayStoreForUpdate()
    }

    logoutButton.visibleOrGone(isLogoutUserFeatureEnabled)
  }

  private fun changeLanguageButtonClicks(): Observable<SettingsEvent> {
    return changeLanguageButton.clicks().map { ChangeLanguage }
  }

  private fun logoutButtonClicks(): Observable<SettingsEvent> {
    return logoutButton.clicks().map { LogoutButtonClicked }
  }

  private fun toggleChangeLanguageFeature() {
    changeLanguageWidgetGroup.visibility = if (isChangeLanguageFeatureEnabled) VISIBLE else GONE
  }

  override fun displayUserDetails(name: String, phoneNumber: String) {
    userName.text = name
    userNumber.text = phoneNumber
  }

  override fun displayCurrentLanguage(language: String) {
    currentLanguage.text = language
  }

  override fun setChangeLanguageButtonVisible() {
    if (isChangeLanguageFeatureEnabled) {
      changeLanguageButton.visibility = VISIBLE
    }
  }

  override fun openLanguageSelectionScreen() {
    router.push(ChangeLanguageScreen.Key())
  }

  override fun displayAppVersion(version: String) {
    appVersion.text = requireContext().getString(R.string.settings_software_version, version)
  }

  override fun showAppUpdateButton() {
    updateAppVersionButton.visibility = VISIBLE
  }

  override fun hideAppUpdateButton() {
    updateAppVersionButton.visibility = GONE
  }

  override fun showConfirmLogoutDialog() {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.settings_logout_dialog_title)
        .setMessage(R.string.settings_logout_dialog_desc)
        .setPositiveButton(R.string.settings_logout_dialog_positive_action) { _, _ ->
          hotEvents.onNext(ConfirmLogoutButtonClicked)
        }
        .setNegativeButton(R.string.settings_logout_dialog_negative_action, null)
        .show()
  }

  override fun restartApp() {
    // TODO: Use process phoenix to restart the app process
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
