package org.simple.clinic.facility.change

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.facility.Facility
import org.simple.clinic.registration.facility.FacilitiesAdapter
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.hideKeyboard
import javax.inject.Inject

class FacilityChangeScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = FacilityChangeScreenKey()
  }

  @Inject
  lateinit var controller: FacilityChangeScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val toolbar by bindView<Toolbar>(R.id.facilitychange_toolbar)
  private val facilityRecyclerView by bindView<RecyclerView>(R.id.facilitychange_list)

  private val recyclerViewAdapter = FacilitiesAdapter()

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable.mergeArray(screenCreates(), facilityClicks())
        .observeOn(Schedulers.io())
        .compose(controller)
        .observeOn(AndroidSchedulers.mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }

    toolbar.setNavigationOnClickListener {
      screenRouter.pop()
    }

    facilityRecyclerView.layoutManager = LinearLayoutManager(context)
    facilityRecyclerView.adapter = recyclerViewAdapter

    // For some reasons, the keyboard stays
    // visible when coming from AppLockScreen.
    hideKeyboard()
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun facilityClicks() = recyclerViewAdapter.facilityClicks.map(::FacilityClicked)

  fun updateFacilities(facilityItems: List<Facility>) {
    recyclerViewAdapter.submitList(facilityItems)
  }

  fun goBack() {
    screenRouter.pop()
  }
}
