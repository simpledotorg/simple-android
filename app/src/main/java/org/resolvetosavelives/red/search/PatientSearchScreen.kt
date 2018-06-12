package org.resolvetosavelives.red.search

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.resolvetosavelives.red.R
import org.resolvetosavelives.red.TheActivity
import org.resolvetosavelives.red.newentry.PatientEntryScreen
import org.resolvetosavelives.red.patient.PatientSearchResult
import org.resolvetosavelives.red.router.screen.ScreenRouter
import org.resolvetosavelives.red.summary.PatientSummaryScreenKey
import org.resolvetosavelives.red.widgets.showKeyboard
import java.util.UUID
import javax.inject.Inject

class PatientSearchScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = PatientSearchScreenKey()
  }

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientSearchScreenController

  private val backButton by bindView<ImageButton>(R.id.patientsearch_back)
  private val searchEditText by bindView<EditText>(R.id.patientsearch_text)
  private val newPatientButton by bindView<Button>(R.id.patientsearch_new_patient)
  private val patientRecyclerView by bindView<RecyclerView>(R.id.patientsearch_recyclerview)
  private val resultsAdapter = PatientSearchResultsAdapter()

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    Observable.merge(searchTextChanges(), newPatientButtonClicks(), backButtonClicks(), searchResultClicks())
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { uiChange -> uiChange(this) }
  }

  private fun searchTextChanges() = RxTextView.textChanges(searchEditText)
      .map(CharSequence::toString)
      .map(::SearchQueryTextChanged)

  private fun newPatientButtonClicks() = RxView.clicks(newPatientButton)
      .map { CreateNewPatientClicked() }

  private fun backButtonClicks() = RxView.clicks(backButton)
      .map { BackButtonClicked() }

  private fun searchResultClicks() = resultsAdapter.itemClicks

  fun showKeyboardOnPhoneNumberField() {
    searchEditText.showKeyboard()
  }

  fun setupSearchResultsList() {
    patientRecyclerView.adapter = resultsAdapter
    patientRecyclerView.layoutManager = LinearLayoutManager(context)
  }

  fun updatePatientSearchResults(patients: List<PatientSearchResult>) {
    resultsAdapter.updateAndNotifyChanges(patients)
  }

  fun openPatientSummaryScreen(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(patientUuid))
  }

  fun openPersonalDetailsEntryScreen() {
    screenRouter.push(PatientEntryScreen.KEY)
  }

  fun goBackToHomeScreen() {
    screenRouter.pop()
  }
}
