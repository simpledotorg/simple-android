package org.simple.clinic.home.report

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenReportBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import javax.inject.Inject

class ReportsScreen : BaseScreen<
    ReportsScreen.Key,
    ScreenReportBinding,
    ReportsModel,
    ReportsEvent,
    ReportsEffect>(), ReportsUi {

  @Inject
  lateinit var effectHandler: ReportsEffectHandler

  private val webViewBackClicks = PublishSubject.create<UiEvent>()

  private val webView
    get() = binding.webView

  private val noReportView
    get() = binding.noReportView

  override fun defaultModel() = ReportsModel.create()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenReportBinding.inflate(layoutInflater, container, false)

  override fun uiRenderer() = ReportsUiRenderer(this)

  override fun events() = Observable
      .mergeArray(webViewBackClicks)
      .compose(ReportAnalyticsEvents())
      .cast<ReportsEvent>()

  override fun createUpdate() = ReportsUpdate()

  override fun createInit() = ReportsInit()

  override fun createEffectHandler() = effectHandler.build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  @SuppressLint("SetJavaScriptEnabled")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    webView.settings.javaScriptEnabled = true
    webView.webViewClient = ReportsWebViewClient(
        backClicked = { webViewBackClicks.onNext(WebBackClicked) }
    )
  }

  override fun showReport(html: String) {
    showWebview(true)
    webView.loadDataWithBaseURL(null, html, "text/html", Charsets.UTF_8.name(), null)
  }

  override fun showNoReportsAvailable() {
    showWebview(false)
    webView.loadUrl("about:blank")
  }

  private fun showWebview(isVisible: Boolean) {
    webView.visibleOrGone(isVisible)
    noReportView.visibleOrGone(isVisible.not())
  }

  interface Injector {
    fun inject(target: ReportsScreen)
  }

  @Parcelize
  class Key : ScreenKey() {

    override val analyticsName = "Reports"

    override fun instantiateFragment() = ReportsScreen()
  }
}
