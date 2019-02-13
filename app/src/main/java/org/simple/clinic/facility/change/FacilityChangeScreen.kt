package org.simple.clinic.facility.change

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mikepenz.itemanimators.SlideUpAlphaAnimator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
import org.simple.clinic.registration.facility.FacilitiesAdapter
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.RecyclerViewUserScrollDetector
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.hideKeyboard
import javax.inject.Inject

class FacilityChangeScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var controller: FacilityChangeScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val toolbar by bindView<Toolbar>(R.id.facilitychange_toolbar)
  private val facilityRecyclerView by bindView<RecyclerView>(R.id.facilitychange_list)
  private val searchEditText by bindView<EditText>(R.id.facilitychange_search)

  private val recyclerViewAdapter = FacilitiesAdapter()

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable.mergeArray(screenCreates(), searchQueryChanges(), facilityClicks())
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
    searchEditText.requestFocus()
    post {
      hideKeyboard()
    }

    hideKeyboardOnListScroll()
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun searchQueryChanges() =
      RxTextView
          .textChanges(searchEditText)
          .map { text -> FacilityChangeSearchQueryChanged(text.toString()) }

  private fun facilityClicks() =
      recyclerViewAdapter
          .facilityClicks
          .map(::FacilityChangeClicked)

  @SuppressLint("CheckResult")
  private fun hideKeyboardOnListScroll() {
    val scrollEvents = RxRecyclerView.scrollEvents(facilityRecyclerView)
    val scrollStateChanges = RxRecyclerView.scrollStateChanges(facilityRecyclerView)

    Observables.combineLatest(scrollEvents, scrollStateChanges)
        .compose(RecyclerViewUserScrollDetector.streamDetections())
        .filter { it.byUser }
        .takeUntil(RxView.detaches(this))
        .subscribe {
          hideKeyboard()
        }
  }

  fun updateFacilities(facilityItems: List<FacilityListItem>, updateType: FacilitiesUpdateType) {
    // Avoid animating the items on their first entry.
    facilityRecyclerView.itemAnimator = when (updateType) {
      FIRST_UPDATE -> null
      SUBSEQUENT_UPDATE -> SlideUpAlphaAnimator()
          .withInterpolator(FastOutSlowInInterpolator())
          .apply { moveDuration = 200 }
    }

    facilityRecyclerView.scrollToPosition(0)
    recyclerViewAdapter.submitList(facilityItems)
  }

  fun goBack() {
    screenRouter.pop()
  }
}
