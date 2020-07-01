package org.simple.clinic.registration.register

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_registration_loading.view.*
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class RegistrationLoadingScreen(
    context: Context,
    attrs: AttributeSet
) : LinearLayout(context, attrs), RegistrationLoadingUi {

  @Inject
  lateinit var controller: RegistrationLoadingScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  override fun onFinishInflate() {
    super.onFinishInflate()

    context.injector<Injector>().inject(this)

    loaderBack.setOnClickListener {
      screenRouter.pop()
    }

    bindUiToController(
        ui = this,
        events = Observable.merge(screenCreates(), retryClicks()),
        controller = controller,
        screenDestroys = detaches().map { ScreenDestroyed() }
    )
  }

  private fun screenCreates() = Observable.just<UiEvent>(ScreenCreated())

  private fun retryClicks() = errorRetryButton
      .clicks()
      .map { RegisterErrorRetryClicked }
      .doAfterNext { showLoader() }

  override fun openHomeScreen() {
    screenRouter.clearHistoryAndPush(HomeScreenKey(), RouterDirection.FORWARD)
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
