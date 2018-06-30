package org.simple.clinic.search

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.rxkotlin.ofType
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.newentry.PatientEntryScreen
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.PatientSummaryCaller.SEARCH
import org.simple.clinic.summary.PatientSummaryScreen
import org.simple.clinic.widgets.showKeyboard
import java.util.UUID
import javax.inject.Inject

class PatientSearchScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientSearchScreenKey()
    const val REQCODE_AGE = 1
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var controller: PatientSearchScreenController

  private val backButton by bindView<ImageButton>(R.id.patientsearch_back)
  private val searchEditText by bindView<EditText>(R.id.patientsearch_text)
  private val ageFilterButton by bindView<Button>(R.id.patientsearch_age_filter_button)
  private val newPatientButton by bindView<Button>(R.id.patientsearch_new_patient)
  private val patientRecyclerView by bindView<RecyclerView>(R.id.patientsearch_recyclerview)
  private val resultsAdapter = PatientSearchResultsAdapter()

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    // TODO: Can we use sealed classes to represent events?
    Observable
        .mergeArray(
            searchQueryChanges(),
            ageFilterToolbarButtonClicks(),
            ageFilterTextChanges(),
            newPatientButtonClicks(),
            backButtonClicks(),
            searchResultClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun searchQueryChanges() = RxTextView.textChanges(searchEditText)
      .map(CharSequence::toString)
      .map(::SearchQueryTextChanged)

  private fun ageFilterToolbarButtonClicks() = RxView.clicks(ageFilterButton)
      .map { SearchQueryAgeFilterClicked() }

  private fun ageFilterTextChanges() = screenRouter.streamScreenResults()
      .ofType<ActivityResult>()
      .filter { it.requestCode == REQCODE_AGE && it.succeeded() }
      .map { PatientSearchAgeFilterSheet.extractResult(it.data!!) }
      .startWith(SearchQueryAgeChanged(""))

  private fun newPatientButtonClicks() = RxView.clicks(newPatientButton)
      .map { CreateNewPatientClicked() }

  private fun backButtonClicks() = RxView.clicks(backButton)
      .map { BackButtonClicked() }

  private fun searchResultClicks() = resultsAdapter.itemClicks

  fun showKeyboardOnSearchEditText() {
    searchEditText.showKeyboard()
  }

  fun showCreatePatientButton(shouldBeShown: Boolean) {
    if (shouldBeShown) {
      newPatientButton.visibility = View.VISIBLE
    } else {
      newPatientButton.visibility = View.GONE
    }
  }

  fun setupSearchResultsList() {
    patientRecyclerView.adapter = resultsAdapter
    patientRecyclerView.layoutManager = LinearLayoutManager(context)
  }

  fun updatePatientSearchResults(patients: List<PatientSearchResult>) {
    resultsAdapter.updateAndNotifyChanges(patients)
  }

  fun openAgeFilterSheet(ageText: String) {
    activity.startActivityForResult(PatientSearchAgeFilterSheet.intent(context, ageText), REQCODE_AGE)
  }

  fun openPatientSummaryScreen(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreen.KEY(patientUuid, SEARCH))
  }

  fun openPersonalDetailsEntryScreen() {
    screenRouter.push(PatientEntryScreen.KEY)
  }

  fun goBackToHomeScreen() {
    screenRouter.pop()
  }
}
