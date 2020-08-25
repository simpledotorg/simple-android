package org.simple.clinic.registration.register

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_registration_loading.view.*
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.di.injector
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.unsafeLazy
import javax.inject.Inject

class RegistrationLoadingScreen(
    context: Context,
    attrs: AttributeSet
) : LinearLayout(context, attrs), RegistrationLoadingUi, RegistrationLoadingUiActions {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var effectHandlerFactory: RegistrationLoadingEffectHandler.Factory

  private val events by unsafeLazy {
    retryClicks()
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate by unsafeLazy {
    val uiRenderer = RegistrationLoadingUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = RegistrationLoadingModel.create(),
        effectHandler = effectHandlerFactory.create(this).build(),
        update = RegistrationLoadingUpdate(),
        init = RegistrationLoadingInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()

    context.injector<Injector>().inject(this)

    loaderBack.setOnClickListener {
      screenRouter.pop()
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

  private fun retryClicks() = errorRetryButton
      .clicks()
      .map { RegisterErrorRetryClicked }
      .doAfterNext { showLoader() }

  override fun openHomeScreen() {
    screenRouter.clearHistoryAndPush(HomeScreenKey, RouterDirection.FORWARD)
  }

  override fun showNetworkError() {
    errorTitle.text = resources.getString(R.string.registrationloader_error_internet_connection_title)
    errorMessage.visibility = View.GONE
    viewSwitcher.showNext()
  }

  override fun showUnexpectedError() {
    errorTitle.text = resources.getString(R.string.registrationloader_error_unexpected_title)
    errorMessage.text = resources.getString(R.string.registrationloader_error_unexpected_message)
    errorMessage.visibility = View.VISIBLE
    viewSwitcher.showNext()
  }

  private fun showLoader() {
    viewSwitcher.showNext()
  }

  interface Injector {
    fun inject(target: RegistrationLoadingScreen)
  }
}
