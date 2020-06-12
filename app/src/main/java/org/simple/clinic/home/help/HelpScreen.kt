package org.simple.clinic.home.help

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_help.view.*
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.help.HelpScreenTryAgainClicked
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.visibleOrGone
import javax.inject.Inject

class HelpScreen(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

  @Inject
  lateinit var controller: HelpScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  @SuppressLint("SetJavaScriptEnabled")
  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }

    toolbar.setNavigationOnClickListener { screenRouter.pop() }

    webView.settings.javaScriptEnabled = true

    context.injector<Injector>().inject(this)

    bindUiToController(
        ui = this,
        events = Observable.merge(screenCreates(), tryAgainClicks()),
        controller = controller,
        screenDestroys = detaches().map { ScreenDestroyed() }
    )
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun tryAgainClicks() = tryAgainButton
      .clicks()
      .map { HelpScreenTryAgainClicked }

  fun showHelp(html: String) {
    webView.visibleOrGone(true)
    progressBar.visibleOrGone(false)
    noContentView.visibleOrGone(false)
    webView.loadDataWithBaseURL(null, html, "text/html", Charsets.UTF_8.name(), null)
  }

  fun showNoHelpAvailable() {
    webView.visibleOrGone(false)
    progressBar.visibleOrGone(false)
    noContentView.visibleOrGone(true)
    webView.loadUrl("about:blank")
  }

  fun showLoadingView() {
    progressBar.visibleOrGone(true)
    noContentView.visibleOrGone(false)
  }

  fun showNetworkErrorMessage() {
    errorMessageTextView.setText(R.string.help_no_connection)
  }

  fun showUnexpectedErrorMessage() {
    errorMessageTextView.setText(R.string.help_something_went_wrong)
  }

  interface Injector {
    fun inject(target: HelpScreen)
  }
}
