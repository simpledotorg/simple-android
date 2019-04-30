package org.simple.clinic.home

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.facility.change.FacilityChangeScreenKey
import org.simple.clinic.home.help.HelpScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.visibleOrGone
import javax.inject.Inject

class HomeScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  private val rootLayout by bindView<ViewGroup>(R.id.home_root)
  private val viewPager by bindView<ViewPager>(R.id.home_viewpager)
  private val facilitySelectButton by bindView<Button>(R.id.home_facility_change_button)
  private val toolBar by bindView<Toolbar>(R.id.home_toolbar)

  @Inject
  lateinit var controller: HomeScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val showHelpMenuItem by lazy(LazyThreadSafetyMode.NONE) {
    toolBar.menu.findItem(R.id.home_actionhelp)
  }

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
    toolBar.inflateMenu(R.menu.home)
    toolBar.setOnMenuItemClickListener { menuItem ->
      when (menuItem.itemId) {
        R.id.home_actionhelp -> {
          screenRouter.push(HelpScreenKey())
          true
        }
        else -> false
      }
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

  fun showHelpButton(isVisible: Boolean) {
    showHelpMenuItem.visibleOrGone(isVisible)
  }
}
