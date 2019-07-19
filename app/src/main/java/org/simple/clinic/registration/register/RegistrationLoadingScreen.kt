package org.simple.clinic.registration.register

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.home.HomeScreenKey
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

class RegistrationLoadingScreen(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

  @Inject
  lateinit var controller: RegistrationLoadingScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  override fun onFinishInflate() {
    super.onFinishInflate()

    TheActivity.component.inject(this)

    bindUiToController(
        ui = this,
        events = Observable.just<UiEvent>(ScreenCreated()),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
  }

  fun openHomeScreen() {
    screenRouter.clearHistoryAndPush(HomeScreenKey(), RouterDirection.FORWARD)
  }

  fun showError() {
    TODO("not implemented")
  }
}
 
