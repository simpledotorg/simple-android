package org.simple.clinic.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.android.synthetic.main.screen_settings.view.*
import org.simple.clinic.BuildConfig
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.settings.changelanguage.ChangeLanguageScreenKey
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class SettingsScreen(
    context: Context,
    attributeSet: AttributeSet
) : LinearLayout(context, attributeSet), SettingsUi, UiActions {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var settingsEffectHandler: SettingsEffectHandler.Factory

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

  private fun changeLanguageButtonClicks(): Observable<SettingsEvent> {
    return RxView.clicks(changeLanguageButton).map { ChangeLanguage }
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)

    toolbar.setNavigationOnClickListener { screenRouter.pop() }

    updateAppVersionButton.setOnClickListener {
      launchPlayStoreForUpdate()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
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
    changeLanguageButton.visibility = View.VISIBLE
  }

  override fun openLanguageSelectionScreen() {
    screenRouter.push(ChangeLanguageScreenKey())
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
