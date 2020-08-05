package org.simple.clinic.searchresultsview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.patient_search_view.view.*
import org.simple.clinic.R
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import javax.inject.Inject

class PatientSearchView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), PatientSearchUi {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientSearchViewController

  val upstreamUiEvents: Subject<UiEvent> = PublishSubject.create()

  private val adapter = ItemAdapter(SearchResultsItemType.DiffCallback())

  private val externalEvents: Subject<UiEvent> = PublishSubject.create()

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    inflate(context, R.layout.patient_search_view, this)
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)
    setupScreen()

    bindUiToController(
        ui = this,
        events = Observable.merge(
            screenCreates(),
            newPatientClicks(),
            searchResultClicks(),
            externalEvents
        ),
        controller = controller,
        screenDestroys = detaches().map { ScreenDestroyed() }
    )
  }

  fun searchWithCriteria(searchCriteria: PatientSearchCriteria) {
    externalEvents.onNext(SearchPatientWithCriteria(searchCriteria))
  }

  private fun setupScreen() {
    resultsRecyclerView.layoutManager = LinearLayoutManager(context)
    resultsRecyclerView.adapter = adapter
  }

  private fun screenCreates(): Observable<UiEvent> =
      Observable.just(SearchResultsViewCreated)

  private fun newPatientClicks() =
      newPatientButton
          .clicks()
          .map { RegisterNewPatientClicked }

  private fun searchResultClicks(): Observable<UiEvent> {
    return adapter
        .itemEvents
        .ofType<SearchResultsItemType.Event.ResultClicked>()
        .map { SearchResultClicked(it.patientUuid) }
  }

  override fun updateSearchResults(
      results: PatientSearchResults
  ) {
    loader.visibleOrGone(isVisible = false)
    newPatientContainer.visibleOrGone(isVisible = true)
    if (results.hasNoResults) {
      setEmptyStateVisible(true)
      adapter.submitList(emptyList())
    } else {
      setEmptyStateVisible(false)
      adapter.submitList(SearchResultsItemType.from(results))
    }
  }

  override fun searchResultClicked(searchResultClickedEvent: SearchResultClicked) {
    upstreamUiEvents.onNext(searchResultClickedEvent)
  }

  private fun setEmptyStateVisible(visible: Boolean) {
    emptyStateView.visibleOrGone(visible)

    newPatientRationaleTextView.setText(when {
      visible -> R.string.patientsearchresults_register_patient_rationale_for_empty_state
      else -> R.string.patientsearchresults_register_patient_rationale
    })
    newPatientButton.setText(when {
      visible -> R.string.patientsearchresults_register_patient_for_empty_state
      else -> R.string.patientsearchresults_register_patient
    })
  }

  override fun registerNewPatient(registerNewPatientEvent: RegisterNewPatient) {
    upstreamUiEvents.onNext(registerNewPatientEvent)
  }

  interface Injector {
    fun inject(target: PatientSearchView)
  }
}
