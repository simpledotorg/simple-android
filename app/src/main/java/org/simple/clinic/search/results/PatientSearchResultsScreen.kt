package org.simple.clinic.search.results

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers.io
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.newentry.PatientEntryScreen
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.PatientSummaryCaller
import org.simple.clinic.summary.PatientSummaryScreen
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import java.util.UUID
import javax.inject.Inject

class PatientSearchResultsScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  companion object {
    val KEY = ::PatientSearchResultsScreenKey
  }

  @Inject
  lateinit var adapter: PatientSearchResultsAdapter

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientSearchResultsController

  private val toolbar by bindView<Toolbar>(R.id.patientsearchresults_toolbar)
  private val recyclerView by bindView<RecyclerView>(R.id.patientsearchresults_results)
  private val newPatientButton by bindView<Button>(R.id.patientsearchresults_new_patient)

  private val queryAgeTextView by lazy {
    // The age View is inflated as a menu so that it forces the toolbar
    // title to get ellipsized instead of overlapping age if it's really long.
    toolbar.inflateMenu(R.menu.patient_search_results)
    toolbar.findViewById<TextView>(R.id.patientsearchresults_query_age)
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)
    setupScreen()

    Observable
        .mergeArray(screenCreates(), newPatientClicks(), adapter.itemClicks)
        .observeOn(io())
        .compose(controller)
        .observeOn(mainThread())
        .takeUntil(RxView.detaches(this))
        .subscribe { it(this) }
  }

  private fun setupScreen() {
    hideKeyboard()
    toolbar.setNavigationOnClickListener {
      screenRouter.pop()
    }

    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = adapter

    val screenKey = screenRouter.key<PatientSearchResultsScreenKey>(this)
    toolbar.title = screenKey.fullName
  }

  private fun screenCreates(): Observable<UiEvent> {
    val screenKey = screenRouter.key<PatientSearchResultsScreenKey>(this)
    return Observable.just(PatientSearchResultsScreenCreated(screenKey))
  }

  private fun newPatientClicks() =
      RxView
          .clicks(newPatientButton)
          .map { CreateNewPatientClicked() }

  fun updateSearchResults(results: List<PatientSearchResult>) {
    adapter.updateAndNotifyChanges(results)
  }

  fun openPatientSummaryScreen(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreen.KEY(patientUuid, PatientSummaryCaller.SEARCH))
  }

  fun openPatientEntryScreen() {
    screenRouter.push(PatientEntryScreen.KEY)
  }

  fun setEmptyStateVisible(visible: Boolean) {
    // TODO.
  }

  fun showComputedAge(age: String) {
    queryAgeTextView.text = age
  }
}
