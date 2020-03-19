package org.simple.clinic.facility.change

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mikepenz.itemanimators.SlideUpAlphaAnimator
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_facility_change.view.*
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
import org.simple.clinic.facility.change.confirm.ConfirmFacilityChangeSheet
import org.simple.clinic.location.LOCATION_PERMISSION
import org.simple.clinic.main.TheActivity
import org.simple.clinic.registration.facility.FacilitiesAdapter
import org.simple.clinic.router.screen.ActivityResult
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
  lateinit var activity: AppCompatActivity

  private val recyclerViewAdapter = FacilitiesAdapter()

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    bindUiToController(
        ui = this,
        events = Observable.merge(
            screenCreates(),
            searchQueryChanges(),
            facilityClicks(),
            locationPermissionChanges()
        ),
        controller = controller,
        screenDestroys = screenDestroys
    )

    toolbarWithSearch.setNavigationOnClickListener {
      screenRouter.pop()
    }
    toolbarWithoutSearch.setNavigationOnClickListener {
      screenRouter.pop()
    }

    facilityList.layoutManager = LinearLayoutManager(context)
    facilityList.adapter = recyclerViewAdapter

    // For some reasons, the keyboard stays
    // visible when coming from AppLockScreen.
    searchEditText.requestFocus()
    post { hideKeyboard() }

    hideKeyboardOnListScroll()
    setupConfirmationSheetResults(screenDestroys)
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private val screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }

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
    val scrollEvents = RxRecyclerView.scrollEvents(facilityList)
    val scrollStateChanges = RxRecyclerView.scrollStateChanges(facilityList)

    Observables.combineLatest(scrollEvents, scrollStateChanges)
        .compose(RecyclerViewUserScrollDetector.streamDetections())
        .filter { it.byUser }
        .takeUntil(RxView.detaches(this))
        .subscribe {
          hideKeyboard()
        }
  }

  @SuppressLint("CheckResult")
  private fun setupConfirmationSheetResults(screenDestroys: Observable<ScreenDestroyed>) {
    screenRouter.streamScreenResults()
        .ofType<ActivityResult>()
        .filter { it.requestCode == OPEN_CONFIRMATION_SHEET && it.succeeded() }
        .takeUntil(screenDestroys)
        .subscribe { goBack() }
  }


  fun updateFacilities(facilityItems: List<FacilityListItem>, updateType: FacilitiesUpdateType) {
    // Avoid animating the items on their first entry.
    facilityList.itemAnimator = when (updateType) {
      FIRST_UPDATE -> null
      SUBSEQUENT_UPDATE -> SlideUpAlphaAnimator()
          .withInterpolator(FastOutSlowInInterpolator())
          .apply { moveDuration = 200 }
    }

    facilityList.scrollToPosition(0)
    recyclerViewAdapter.submitList(facilityItems)
  }

  fun goBack() {
    screenRouter.pop()
  }

  fun showProgressIndicator() {
    progress.visibility = VISIBLE
  }

  fun hideProgressIndicator() {
    progress.visibility = GONE
  }

  fun showToolbarWithSearchField() {
    toolbarContainer.displayedChildResId = R.id.toolbarWithSearch
  }

  fun showToolbarWithoutSearchField() {
    toolbarContainer.displayedChildResId = R.id.toolbarWithoutSearch
  }

  fun openConfirmationSheet(facility: Facility) {
    activity.startActivityForResult(
        ConfirmFacilityChangeSheet.intent(context, facility),
        OPEN_CONFIRMATION_SHEET
    )
  }

  companion object {
    private const val OPEN_CONFIRMATION_SHEET = 1210
  }
}
