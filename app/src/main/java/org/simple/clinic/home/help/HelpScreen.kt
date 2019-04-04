package org.simple.clinic.home.help

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.WebView
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.visibleOrGone
import java.net.URI
import javax.inject.Inject

class HelpScreen(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

  @Inject
  lateinit var controller: HelpScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val webView by bindView<WebView>(R.id.help_webview)
  private val noContentView by bindView<View>(R.id.help_no_content)
  private val toolbar by bindView<Toolbar>(R.id.help_toolbar)

  @SuppressLint("SetJavaScriptEnabled", "CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()

    if (isInEditMode) {
      return
    }

    toolbar.setNavigationOnClickListener { screenRouter.pop() }

    webView.settings.javaScriptEnabled = true

    TheActivity.component.inject(this)

    val screenDestroys = RxView
        .detaches(this)
        .map { ScreenDestroyed() }

    Observable.merge(screenCreates(), screenDestroys)
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(screenDestroys)
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  fun showHelp(uri: URI) {
    showWebview(true)
    webView.loadUrl(uri.toString())
  }

  fun showNoHelpAvailable() {
    showWebview(false)
    webView.loadUrl("about:blank")
  }

  private fun showWebview(isVisible: Boolean) {
    webView.visibleOrGone(isVisible)
    noContentView.visibleOrGone(isVisible.not())
  }

  fun showLoadingView(isVisible: Boolean) {
    // TODO
  }

  fun showNetworkErrorMessage() {
    // TODO
  }
}
