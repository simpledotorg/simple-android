package org.simple.clinic.home.help

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding3.view.clicks
import com.spotify.mobius.functions.Consumer
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenHelpBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.widgets.visibleOrGone
import javax.inject.Inject

class HelpScreen : BaseScreen<
    HelpScreen.Key,
    ScreenHelpBinding,
    HelpScreenModel,
    HelpScreenEvent,
    HelpScreenEffect,
    Unit>(), HelpScreenUi, HelpScreenUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: HelpScreenEffectHandler.Factory

  private val toolbar
    get() = binding.toolbar

  private val webView
    get() = binding.webView

  private val tryAgainButton
    get() = binding.tryAgainButton

  private val progressBar
    get() = binding.progressBar

  private val noContentView
    get() = binding.noContentView

  private val errorMessageTextView
    get() = binding.errorMessageTextView

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  @SuppressLint("SetJavaScriptEnabled")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar.setNavigationOnClickListener { router.pop() }

    webView.settings.javaScriptEnabled = true
  }

  override fun defaultModel() = HelpScreenModel.create()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) = ScreenHelpBinding
      .inflate(layoutInflater, container, false)

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Unit>) =
      effectHandlerFactory.create(this).build()

  override fun createInit() = HelpScreenInit()

  override fun createUpdate() = HelpScreenUpdate()

  override fun events(): Observable<HelpScreenEvent> {
    return tryAgainClicks()
        .compose(ReportAnalyticsEvents())
        .cast<HelpScreenEvent>()
  }

  override fun uiRenderer() = HelpScreenUiRenderer(this)

  private fun tryAgainClicks() = tryAgainButton
      .clicks()
      .map { HelpScreenTryAgainClicked }

  override fun showHelp(html: String) {
    webView.visibleOrGone(true)
    progressBar.visibleOrGone(false)
    noContentView.visibleOrGone(false)
    webView.loadDataWithBaseURL(null, html, "text/html", Charsets.UTF_8.name(), null)
  }

  override fun showNoHelpAvailable() {
    webView.visibleOrGone(false)
    progressBar.visibleOrGone(false)
    noContentView.visibleOrGone(true)
    webView.loadUrl("about:blank")
  }

  override fun showLoadingView() {
    progressBar.visibleOrGone(true)
    noContentView.visibleOrGone(false)
  }

  override fun showNetworkErrorMessage() {
    errorMessageTextView.setText(R.string.help_no_connection)
  }

  override fun showUnexpectedErrorMessage() {
    errorMessageTextView.setText(R.string.help_something_went_wrong)
  }

  interface Injector {
    fun inject(target: HelpScreen)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Help"
  ) : ScreenKey() {

    override fun instantiateFragment() = HelpScreen()
  }
}
