package org.simple.clinic.home

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import kotlinx.android.synthetic.main.screen_home.view.*
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.facility.change.FacilityChangeScreenKey
import org.simple.clinic.home.help.HelpScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.settings.SettingsScreenKey
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.hideKeyboard
import javax.inject.Inject

class HomeScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var controller: HomeScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    setupToolBar()

    bindUiToController(
        ui = this,
        events = Observable.merge(screenCreates(), facilitySelectionClicks()),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )

    // Keyboard stays open after login finishes, not sure why.
    rootLayout.hideKeyboard()

    viewPager.adapter = HomePagerAdapter(context)

    // The WebView in "Progress" tab is expensive to load. Pre-instantiating
    // it when the app starts reduces its time-to-display.
    viewPager.offscreenPageLimit = HomeTab.REPORTS.ordinal - HomeTab.PATIENTS.ordinal
  }

  private fun setupToolBar() {
    home_toolbar.apply {
      inflateMenu(R.menu.home)
      setOnMenuItemClickListener { menuItem ->
        when (menuItem.itemId) {
          R.id.openSettings -> {
            screenRouter.push(SettingsScreenKey())
            true
          }
          else -> false
        }
      }
    }
    helpButton.setOnClickListener {
      screenRouter.push(HelpScreenKey())
    }
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun facilitySelectionClicks() = RxView
      .clicks(facilitySelectButton)
      .map { HomeFacilitySelectionClicked() }

  fun setFacility(facilityName: String) {
    facilitySelectButton.text = facilityName
  }

  fun openFacilitySelection() {
    screenRouter.push(FacilityChangeScreenKey())
  }
}
