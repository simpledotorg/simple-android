package org.simple.clinic.allpatientsinfacility

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
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
import org.simple.clinic.allpatientsinfacility.migration.ExposesUiEvents
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import java.util.Locale
import javax.inject.Inject

class AllPatientsInFacilityView(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet), AllPatientsInFacilityUi, ExposesUiEvents {

  private val searchResultsAdapter by unsafeLazy {
    AllPatientsInFacilityListAdapter(AllPatientsInFacilityListItemCallback(), locale)
  }

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var userSession: UserSession

  @Inject
  lateinit var facilityRepository: FacilityRepository

  @Inject
  lateinit var patientRepository: PatientRepository

  @Inject
  lateinit var schedulersProvider: SchedulersProvider

  private val viewRenderer by unsafeLazy { AllPatientsInFacilityUiRenderer(this) }

  private val downstreamUiEvents = PublishSubject.create<UiEvent>()

  override val uiEvents: Observable<UiEvent> = downstreamUiEvents.hide()

  private val delegate by unsafeLazy {
    val effectHandler = AllPatientsInFacilityEffectHandler
        .createEffectHandler(userSession, facilityRepository, patientRepository, schedulersProvider)

    MobiusDelegate(
        AllPatientsInFacilityModel.FETCHING_PATIENTS,
        ::allPatientsInFacilityInit,
        ::allPatientsInFacilityUpdate,
        effectHandler,
        viewRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    TheActivity.component.inject(this)

    setupAllPatientsList()
    setupInitialViewVisibility()
    forwardListEventsToDownstream()
    delegate.prepare()
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

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state as Bundle?))
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
}
