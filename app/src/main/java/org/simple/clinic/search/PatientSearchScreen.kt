package org.simple.clinic.search

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_patient_search.view.*
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilitySearchResultClicked
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityView
import org.simple.clinic.bindUiToController
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.results.PatientSearchResultsScreenKey
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
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

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    backButton.setOnClickListener {
      screenRouter.pop()
    }

    bindUiToController(
        ui = this,
        events = Observable.merge(
            nameChanges(),
            searchClicks(),
            patientClickEvents()
        ),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
  }

  private fun nameChanges() =
      RxTextView
          .textChanges(fullNameEditText)
          .map(CharSequence::toString)
          .map(::SearchQueryNameChanged)

  private fun searchClicks(): Observable<SearchClicked> {
    val imeSearchClicks = RxTextView
        .editorActionEvents(fullNameEditText)
        .filter { it.actionId() == EditorInfo.IME_ACTION_SEARCH }

    return RxView
        .clicks(searchButtonFrame.button)
        .mergeWith(imeSearchClicks)
        .map { SearchClicked() }
  }

  private fun patientClickEvents(): Observable<UiEvent> {
    return (allPatientsView as AllPatientsInFacilityView)
        .uiEvents
        .ofType<AllPatientsInFacilitySearchResultClicked>()
        .map { PatientItemClicked(it.patientUuid) }
  }

  fun showSearchButtonAsEnabled() {
    searchButtonFrame.isEnabled = true
  }

  fun showSearchButtonAsDisabled() {
    searchButtonFrame.isEnabled = false
  }

  fun openPatientSearchResultsScreen(name: String) {
    screenRouter.push(PatientSearchResultsScreenKey(name))
  }

  fun setEmptyFullNameErrorVisible(visible: Boolean) {
    fullNameInputLayout.error = if (visible) {
      resources.getString(R.string.patientsearch_error_empty_fullname)
    } else {
      null
    }
    fullNameInputLayout.isErrorEnabled = visible
  }

  fun openPatientSummary(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(
        patientUuid = patientUuid,
        intention = OpenIntention.ViewExistingPatient,
        screenCreatedTimestamp = Instant.now(utcClock)
    ))
  }
}
