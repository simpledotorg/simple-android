package org.simple.clinic.registration.facility

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mikepenz.itemanimators.SlideUpAlphaAnimator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.facility.Facility
import org.simple.clinic.home.HomeScreen
import org.simple.clinic.router.screen.RouterDirection
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.hideKeyboard
import javax.inject.Inject

class RegistrationFacilitySelectionScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = RegistrationFacilitySelectionScreenKey()
  }

  @Inject
  lateinit var controller: RegistrationFacilitySelectionScreenController

  @Inject
  lateinit var screenRouter: ScreenRouter

  private val toolbar by bindView<Toolbar>(R.id.registrationfacilities_toolbar)
  private val searchEditText by bindView<EditText>(R.id.registrationfacilities_search)
  private val facilityRecyclerView by bindView<RecyclerView>(R.id.registrationfacilities_list)
  private val progressView by bindView<View>(R.id.registrationfacilities_progress)
  private val errorContainer by bindView<ViewGroup>(R.id.registrationfacilities_error_container)
  private val errorTitleTextView by bindView<TextView>(R.id.registrationfacilities_error_title)
  private val errorMessageTextView by bindView<TextView>(R.id.registrationfacilities_error_message)
  private val errorRetryButton by bindView<Button>(R.id.registrationfacilities_error_retry)

  private val recyclerViewAdapter = FacilitiesAdapter()

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    Observable.mergeArray(screenCreates(), searchQueryChanges(), retryClicks(), facilityClicks())
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

    // Filtering facilities is optional. Doing it
    // without adding a post{} block doesn't work.
    post {
      hideKeyboard()
    }
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

  private fun facilityClicks() = recyclerViewAdapter.facilityClicks.map(::RegistrationFacilityClicked)

  fun showProgressIndicator() {
    progressView.visibility = VISIBLE
  }

  fun hideProgressIndicator() {
    progressView.visibility = GONE
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

  fun updateFacilities(facilityItems: List<Facility>, isFirstUpdate: Boolean) {
    // Avoid animating the items on their first entry.
    if (isFirstUpdate) {
      facilityRecyclerView.itemAnimator = null
    } else {
      facilityRecyclerView.itemAnimator = SlideUpAlphaAnimator()
          .withInterpolator(FastOutSlowInInterpolator())
          .apply { moveDuration = 200 }
    }

    facilityRecyclerView.scrollToPosition(0)
    recyclerViewAdapter.submitList(facilityItems)
  }

  fun openHomeScreen() {
    screenRouter.clearHistoryAndPush(HomeScreen.KEY, RouterDirection.FORWARD)
  }
}
