package org.simple.clinic.home

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.facility.change.FacilityChangeScreen
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.hideKeyboard
import javax.inject.Inject

class HomeScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = HomeScreenKey()
  }

  private val rootLayout by bindView<ViewGroup>(R.id.home_root)
  private val viewPager by bindView<ViewPager>(R.id.home_viewpager)
  private val facilitySelectButton by bindView<Button>(R.id.home_facility_change_button)

  @Inject
  lateinit var controller: HomeScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    Observable.merge(screenCreates(), facilitySelectionClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }

    // Keyboard stays open after login finishes, not sure why.
    rootLayout.hideKeyboard()

    viewPager.adapter = HomePagerAdapter(context)
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun facilitySelectionClicks() = RxView
      .clicks(facilitySelectButton)
      .map { HomeFacilitySelectionClicked() }

  fun setFacility(facilityName: String) {
    facilitySelectButton.text = facilityName
  }

  fun openFacilitySelection() {
    screenRouter.push(FacilityChangeScreen.KEY)
  }
}
