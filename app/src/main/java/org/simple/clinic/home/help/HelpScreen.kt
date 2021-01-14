package org.simple.clinic.home.help

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.LinearLayout
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenHelpBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.visibleOrGone
import javax.inject.Inject

class HelpScreen(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), HelpScreenUi, HelpScreenUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandlerFactory: HelpScreenEffectHandler.Factory

  private var binding: ScreenHelpBinding? = null

  private val toolbar
    get() = binding!!.toolbar

  private val webView
    get() = binding!!.webView

  private val tryAgainButton
    get() = binding!!.tryAgainButton

  private val progressBar
    get() = binding!!.progressBar

  private val noContentView
    get() = binding!!.noContentView

  private val errorMessageTextView
    get() = binding!!.errorMessageTextView

  private val events by unsafeLazy {
    tryAgainClicks()
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = HelpScreenUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = HelpScreenModel.create(),
        init = HelpScreenInit(),
        update = HelpScreenUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
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

  @SuppressLint("SetJavaScriptEnabled")
  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }

    binding = ScreenHelpBinding.bind(this)

    context.injector<Injector>().inject(this)

    toolbar.setNavigationOnClickListener { router.pop() }

    webView.settings.javaScriptEnabled = true
  }

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
}
