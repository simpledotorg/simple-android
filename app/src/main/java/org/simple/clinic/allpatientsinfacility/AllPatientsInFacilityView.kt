package org.simple.clinic.allpatientsinfacility

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.view_allpatientsinfacility.view.*
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityListItem.AllPatientsInFacilityListItemCallback
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityListItem.Event.SearchResultClicked
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import java.util.Locale
import javax.inject.Inject

class AllPatientsInFacilityView(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet), AllPatientsInFacilityUi {

  private val searchResultsAdapter by unsafeLazy {
    AllPatientsInFacilityListAdapter(AllPatientsInFacilityListItemCallback(), locale)
  }

  @Inject
  lateinit var locale: Locale

  private val downstreamUiEvents = PublishSubject.create<UiEvent>()

  val uiEvents: Observable<UiEvent> = downstreamUiEvents.hide()

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    setupAllPatientsList()
    setupInitialViewVisibility()
    forwardListEventsToDownstream()
  }

  @SuppressLint("CheckResult")
  private fun forwardListEventsToDownstream() {
    Observable.merge(searchResultClicks(), listScrollEvents())
        .takeUntil(RxView.detaches(this))
        .subscribe(downstreamUiEvents::onNext)
  }

  private fun searchResultClicks(): Observable<UiEvent> {
    return searchResultsAdapter
        .itemEvents
        .ofType<SearchResultClicked>()
        .map { it.patientUuid }
        .map(::AllPatientsInFacilitySearchResultClicked)
  }

  private fun listScrollEvents(): Observable<UiEvent> {
    return RxRecyclerView
        .scrollStateChanges(patientsList)
        .filter { it == RecyclerView.SCROLL_STATE_DRAGGING }
        .map { AllPatientsInFacilityListScrolled }
  }

  private fun setupInitialViewVisibility() {
    patientsList.visibility = View.GONE
    noPatientsContainer.visibility = View.GONE
  }

  private fun setupAllPatientsList() {
    patientsList.apply {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
      adapter = searchResultsAdapter
    }
  }

  override fun showNoPatientsFound(facilityName: String) {
    patientsList.visibility = View.GONE
    noPatientsContainer.visibility = View.VISIBLE
    noPatientsLabel.text = resources.getString(R.string.allpatientsinfacility_nopatients_title, facilityName)
  }

  override fun showPatients(facilityUiState: FacilityUiState, patientSearchResults: List<PatientSearchResultUiState>) {
    patientsList.visibility = View.VISIBLE
    noPatientsContainer.visibility = View.GONE
    val listItems = AllPatientsInFacilityListItem.mapSearchResultsToListItems(facilityUiState, patientSearchResults)
    searchResultsAdapter.submitList(listItems)
  }
}
