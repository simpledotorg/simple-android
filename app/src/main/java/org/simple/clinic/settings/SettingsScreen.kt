package org.simple.clinic.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import org.simple.clinic.BuildConfig
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenSettingsBinding
import org.simple.clinic.di.injector
import org.simple.clinic.feature.Feature
import org.simple.clinic.feature.Features
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.settings.changelanguage.ChangeLanguageScreenKey
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class SettingsScreen(
    context: Context,
    attributeSet: AttributeSet
) : LinearLayout(context, attributeSet), SettingsUi, UiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var settingsEffectHandler: SettingsEffectHandler.Factory

  @Inject
  lateinit var features: Features

  private var binding: ScreenSettingsBinding? = null

  private val changeLanguageButton
    get() = binding!!.changeLanguageButton

  private val toolbar
    get() = binding!!.toolbar

  private val updateAppVersionButton
    get() = binding!!.updateAppVersionButton

  private val userName
    get() = binding!!.userName

  private val userNumber
    get() = binding!!.userNumber

  private val currentLanguage
    get() = binding!!.currentLanguage

  private val appVersion
    get() = binding!!.appVersion

  private val changeLanguageWidgetGroup
    get() = binding!!.changeLanguageWidgetGroup

  private val uiRenderer: SettingsUiRenderer = SettingsUiRenderer(this)

  private val events: Observable<SettingsEvent> by unsafeLazy {
    changeLanguageButtonClicks()
        .compose(ReportAnalyticsEvents())
        .cast<SettingsEvent>()
  }

  private val delegate: MobiusDelegate<SettingsModel, SettingsEvent, SettingsEffect> by unsafeLazy {
    MobiusDelegate.forView(
        events = events,
        defaultModel = SettingsModel.default(BuildConfig.APPLICATION_ID),
        init = SettingsInit(),
        update = SettingsUpdate(),
        effectHandler = settingsEffectHandler.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  private val isChangeLanguageFeatureEnabled by unsafeLazy { features.isEnabled(Feature.ChangeLanguage) }

  private fun changeLanguageButtonClicks(): Observable<SettingsEvent> {
    return changeLanguageButton.clicks().map { ChangeLanguage }
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    binding = ScreenSettingsBinding.bind(this)

    context.injector<Injector>().inject(this)

    toggleChangeLanguageFeature()
    toolbar.setNavigationOnClickListener { router.pop() }

    updateAppVersionButton.setOnClickListener {
      launchPlayStoreForUpdate()
    }
  }

  private fun toggleChangeLanguageFeature() {
    changeLanguageWidgetGroup.visibility = if (isChangeLanguageFeatureEnabled) VISIBLE else GONE
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
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
      changeLanguageButton.visibility = View.VISIBLE
    }
  }

  override fun openLanguageSelectionScreen() {
    router.push(ChangeLanguageScreenKey().wrap())
  }

  override fun displayAppVersion(version: String) {
    appVersion.text = context.getString(R.string.settings_software_version, version)
  }

  override fun showAppUpdateButton() {
    updateAppVersionButton.visibility = View.VISIBLE
  }

  override fun hideAppUpdateButton() {
    updateAppVersionButton.visibility = View.GONE
  }

  private fun launchPlayStoreForUpdate() {
    val intent = Intent(Intent.ACTION_VIEW).apply {
      data = Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
    }
    context.startActivity(intent)
  }

  interface Injector {
    fun inject(target: SettingsScreen)
  }
}
