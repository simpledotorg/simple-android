package org.simple.clinic.registration.facility

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
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
import kotlinx.android.synthetic.main.screen_registration_facility_selection.view.*
import org.simple.clinic.R
import org.simple.clinic.main.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.facility.change.FacilitiesUpdateType
import org.simple.clinic.facility.change.FacilitiesUpdateType.FIRST_UPDATE
import org.simple.clinic.facility.change.FacilitiesUpdateType.SUBSEQUENT_UPDATE
import org.simple.clinic.facility.change.FacilityListItem
import org.simple.clinic.location.LOCATION_PERMISSION
import org.simple.clinic.registration.register.RegistrationLoadingScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.widgets.RecyclerViewUserScrollDetector
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.displayedChildResId
import org.simple.clinic.widgets.hideKeyboard
import javax.inject.Inject

class RegistrationFacilitySelectionScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var controller: RegistrationFacilitySelectionScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: AppCompatActivity

  private val recyclerViewAdapter = FacilitiesAdapter()

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    bindUiToController(
        ui = this,
        events = Observable.mergeArray(
            screenCreates(),
            searchQueryChanges(),
            retryClicks(),
            facilityClicks(),
            locationPermissionChanges()
        ),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )

    toolbarViewWithSearch.setNavigationOnClickListener {
      screenRouter.pop()
    }
    toolbarViewWithoutSearch.setNavigationOnClickListener {
      screenRouter.pop()
    }

    facilityRecyclerView.layoutManager = LinearLayoutManager(context)
    facilityRecyclerView.adapter = recyclerViewAdapter

    searchEditText.requestFocus()

    // Hiding the keyboard without adding a post{} block doesn't seem to work.
    post { hideKeyboard() }
    hideKeyboardOnListScroll()
  }

  private fun screenCreates() = Observable.just(ScreenCreated())

  private fun searchQueryChanges() =
      RxTextView
          .textChanges(searchEditText)
          .map { text -> RegistrationFacilitySearchQueryChanged(text.toString()) }

  private fun retryClicks() =
      RxView
          .clicks(errorRetryButton)
          .map { RegistrationFacilitySelectionRetryClicked() }

  private fun facilityClicks() =
      recyclerViewAdapter
          .facilityClicks
          .map(::RegistrationFacilityClicked)

  private fun locationPermissionChanges(): Observable<UiEvent> {
    val permissionResult = RuntimePermissions.check(activity, LOCATION_PERMISSION)
    return Observable.just(RegistrationFacilityLocationPermissionChanged(permissionResult))
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

  fun showProgressIndicator() {
    progressView.visibility = VISIBLE
  }

  fun hideProgressIndicator() {
    progressView.visibility = GONE
  }

  fun showToolbarWithSearchField() {
    toolbarViewFlipper.displayedChildResId = R.id.toolbarViewWithSearch
  }

  fun showToolbarWithoutSearchField() {
    toolbarViewFlipper.displayedChildResId = R.id.toolbarViewWithoutSearch
  }

  fun showNetworkError() {
    errorContainer.visibility = View.VISIBLE
    errorMessageTextView.visibility = View.GONE
    errorTitleTextView.setText(R.string.registrationfacilities_error_internet_connection_title)
  }

  fun showUnexpectedError() {
    errorContainer.visibility = View.VISIBLE
    errorMessageTextView.visibility = View.VISIBLE
    errorTitleTextView.setText(R.string.registrationfacilities_error_unexpected_title)
    errorMessageTextView.setText(R.string.registrationfacilities_error_unexpected_message)
  }

  fun hideError() {
    errorContainer.visibility = View.GONE
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

  fun openRegistrationScreen() {
    screenRouter.push(RegistrationLoadingScreenKey())
  }
}
