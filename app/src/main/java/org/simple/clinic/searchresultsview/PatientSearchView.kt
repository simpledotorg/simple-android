package org.simple.clinic.searchresultsview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.facility.Facility
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.visibleOrGone
import javax.inject.Inject

class PatientSearchView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientSearchViewController

  @Inject
  lateinit var utcClock: UtcClock

  val downstreamUiEvents: Subject<UiEvent> = PublishSubject.create()
  val upstreamUiEvents: Subject<UiEvent> = PublishSubject.create()

  private val adapter = GroupAdapter<ViewHolder>()

  private val recyclerView by bindView<RecyclerView>(R.id.searchresults_results)
  private val emptyStateView by bindView<View>(R.id.searchresults_empty_state)
  private val newPatientRationaleTextView by bindView<TextView>(R.id.searchresults_new_patient_rationale)
  private val newPatientButton by bindView<Button>(R.id.searchresults_new_patient)

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
    recyclerView.layoutManager = LinearLayoutManager(context)
    recyclerView.adapter = adapter
  }

  private fun screenCreates(): Observable<UiEvent> =
      Observable.just(SearchResultsViewCreated)

  private fun newPatientClicks() =
      RxView
          .clicks(newPatientButton)
          .map { RegisterNewPatientClicked }

  fun updateSearchResults(
      results: PatientSearchResults,
      currentFacility: Facility
  ) {
    if (results.hasNoResults) {
      setEmptyStateVisible(true)
      adapter.update(emptyList())
    } else {
      setEmptyStateVisible(false)
      SearchResultsItemType
          .from(results, currentFacility)
          .let { listItems ->
            listItems.forEach { it.uiEvents = downstreamUiEvents }
            adapter.update(listItems)
          }
    }
  }

  fun searchResultClicked(searchResultClickedEvent: SearchResultClicked) {
    upstreamUiEvents.onNext(searchResultClickedEvent)
  }

  fun setEmptyStateVisible(visible: Boolean) {
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
