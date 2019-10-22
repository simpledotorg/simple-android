package org.simple.clinic.searchresultsview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding2.view.RxView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.patient_search_view.view.*
import org.simple.clinic.R
import org.simple.clinic.main.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import javax.inject.Inject

class PatientSearchView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientSearchViewController

  val downstreamUiEvents: Subject<UiEvent> = PublishSubject.create()
  val upstreamUiEvents: Subject<UiEvent> = PublishSubject.create()

  private val adapter = GroupAdapter<ViewHolder>()

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    inflate(context, R.layout.patient_search_view, this)
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)
    setupScreen()

    bindUiToController(
        ui = this,
        events = Observable.merge(
            screenCreates(),
            newPatientClicks(),
            downstreamUiEvents
        ),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
  }

  private fun setupScreen() {
    resultsRecyclerView.layoutManager = LinearLayoutManager(context)
    resultsRecyclerView.adapter = adapter
  }

  private fun screenCreates(): Observable<UiEvent> =
      Observable.just(SearchResultsViewCreated)

  private fun newPatientClicks() =
      RxView
          .clicks(newPatientButton)
          .map { RegisterNewPatientClicked }

  fun updateSearchResults(
      results: PatientSearchResults
  ) {
    loader.visibleOrGone(isVisible = false)
    newPatientContainer.visibleOrGone(isVisible = true)
    if (results.hasNoResults) {
      setEmptyStateVisible(true)
      adapter.update(emptyList())
    } else {
      setEmptyStateVisible(false)
      SearchResultsItemType
          .from(results)
          .let { listItems ->
            listItems.forEach { it.uiEvents = downstreamUiEvents }
            adapter.update(listItems)
          }
    }
  }

  fun searchResultClicked(searchResultClickedEvent: SearchResultClicked) {
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

  fun registerNewPatient(registerNewPatientEvent: RegisterNewPatient) {
    upstreamUiEvents.onNext(registerNewPatientEvent)
  }
}
