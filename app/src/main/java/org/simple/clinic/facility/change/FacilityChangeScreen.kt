package org.simple.clinic.facility.change

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.ViewFlipper
import androidx.appcompat.widget.Toolbar
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mikepenz.itemanimators.SlideUpAlphaAnimator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
import org.simple.clinic.location.LOCATION_PERMISSION
import org.simple.clinic.registration.facility.FacilitiesAdapter
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.widgets.RecyclerViewUserScrollDetector
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.displayedChildResId
import org.simple.clinic.widgets.hideKeyboard
import javax.inject.Inject

class FacilityChangeScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var controller: FacilityChangeScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: TheActivity

  private val toolbarViewFlipper by bindView<ViewFlipper>(R.id.facilitychange_toolbar_container)
  private val toolbarViewWithSearch by bindView<Toolbar>(R.id.facilitychange_toolbar_with_search)
  private val toolbarViewWithoutSearch by bindView<Toolbar>(R.id.facilitychange_toolbar_without_search)
  private val progressView by bindView<View>(R.id.facilitychange_progress)
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

    val screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }

    Observable
        .mergeArray(
            screenCreates(),
            screenDestroys,
            searchQueryChanges(),
            facilityClicks(),
            locationPermissionChanges())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(screenDestroys)
        .subscribe { uiChange -> uiChange(this) }

    toolbarViewWithSearch.setNavigationOnClickListener {
      screenRouter.pop()
    }
    toolbarViewWithoutSearch.setNavigationOnClickListener {
      screenRouter.pop()
    }

    facilityRecyclerView.layoutManager = LinearLayoutManager(context)
    facilityRecyclerView.adapter = recyclerViewAdapter

    // For some reasons, the keyboard stays
    // visible when coming from AppLockScreen.
    searchEditText.requestFocus()
    post { hideKeyboard() }

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

  private fun locationPermissionChanges(): Observable<UiEvent> {
    val permissionResult = RuntimePermissions.check(activity, LOCATION_PERMISSION)
    return Observable.just(FacilityChangeLocationPermissionChanged(permissionResult))
  }

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

  fun showProgressIndicator() {
    progressView.visibility = VISIBLE
  }

  fun hideProgressIndicator() {
    progressView.visibility = GONE
  }

  fun showToolbarWithSearchField() {
    toolbarViewFlipper.displayedChildResId = R.id.facilitychange_toolbar_with_search
  }

  fun showToolbarWithoutSearchField() {
    toolbarViewFlipper.displayedChildResId = R.id.facilitychange_toolbar_without_search
  }
}
