package org.simple.clinic.search

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_patient_search.view.*
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.allpatientsinfacility_old.AllPatientsInFacilityListScrolled
import org.simple.clinic.allpatientsinfacility_old.AllPatientsInFacilitySearchResultClicked
import org.simple.clinic.allpatientsinfacility_old.AllPatientsInFacilityView
import org.simple.clinic.bindUiToController
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.results.PatientSearchResultsScreenKey
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class PatientSearchScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var controller: PatientSearchScreenController

  @Inject
  lateinit var utcClock: UtcClock

  private val allPatientsInFacilityView by unsafeLazy {
    allPatientsView as AllPatientsInFacilityView
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    backButton.setOnClickListener {
      screenRouter.pop()
    }
    searchQueryEditText.showKeyboard()

    val screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    hideKeyboardWhenAllPatientsListIsScrolled(screenDestroys)

    bindUiToController(
        ui = this,
        events = Observable.merge(
            searchTextChanges(),
            searchClicks(),
            patientClickEvents()
        ),
        controller = controller,
        screenDestroys = screenDestroys
    )
  }

  private fun searchTextChanges(): Observable<UiEvent> {
    return RxTextView
        .textChanges(searchQueryEditText)
        .map(CharSequence::toString)
        .map(::SearchQueryTextChanged)
  }

  private fun searchClicks(): Observable<SearchClicked> {
    val imeSearchClicks = RxTextView
        .editorActionEvents(searchQueryEditText)
        .filter { it.actionId() == EditorInfo.IME_ACTION_SEARCH }

    return RxView
        .clicks(searchButtonFrame.button)
        .mergeWith(imeSearchClicks)
        .map { SearchClicked() }
  }

  private fun patientClickEvents(): Observable<UiEvent> {
    return allPatientsInFacilityView
        .uiEvents
        .ofType<AllPatientsInFacilitySearchResultClicked>()
        .map { PatientItemClicked(it.patientUuid) }
  }

  @Suppress("CheckResult")
  private fun hideKeyboardWhenAllPatientsListIsScrolled(screenDestroys: Observable<ScreenDestroyed>) {
    allPatientsInFacilityView
        .uiEvents
        .ofType<AllPatientsInFacilityListScrolled>()
        .takeUntil(screenDestroys)
        .subscribe { hideKeyboard() }
  }

  fun openSearchResultsScreen(criteria: PatientSearchCriteria) {
    screenRouter.push(PatientSearchResultsScreenKey(criteria))
  }

  fun setEmptyTextFieldErrorVisible(visible: Boolean) {
    searchQueryEditText.error = if (visible) {
      resources.getString(R.string.patientsearch_error_empty_fullname)
    } else null
  }

  fun openPatientSummary(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(
        patientUuid = patientUuid,
        intention = OpenIntention.ViewExistingPatient,
        screenCreatedTimestamp = Instant.now(utcClock)
    ))
  }

  fun showAllPatientsInFacility() {
    allPatientsView.visibility = View.VISIBLE
  }

  fun hideAllPatientsInFacility() {
    allPatientsView.visibility = View.GONE
  }

  fun showSearchButton() {
    searchButtonFrame.visibility = View.VISIBLE
  }

  fun hideSearchButton() {
    searchButtonFrame.visibility = View.GONE
  }
}
