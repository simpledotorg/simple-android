package org.simple.clinic.allpatientsinfacility

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityListItem.AllPatientsInFacilityListItemCallback
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityListItem.Event.SearchResultClicked
import org.simple.clinic.databinding.ListAllpatientsinfacilityFacilityHeaderBinding
import org.simple.clinic.databinding.ListPatientSearchOldBinding
import org.simple.clinic.databinding.ViewAllpatientsinfacilityBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.platform.crash.CrashReporter
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import java.util.Locale
import javax.inject.Inject

class AllPatientsInFacilityView(
    context: Context,
    attributeSet: AttributeSet
) : FrameLayout(context, attributeSet), AllPatientsInFacilityUi {

  private val searchResultsAdapter by unsafeLazy {
    AllPatientsInFacilityListAdapter(
        diffCallback = AllPatientsInFacilityListItemCallback(),
        bindings = mapOf(
            R.layout.list_allpatientsinfacility_facility_header to { layoutInflater, parent ->
              ListAllpatientsinfacilityFacilityHeaderBinding.inflate(layoutInflater, parent, false)
            },
            R.layout.list_patient_search_old to { layoutInflater, parent ->
              ListPatientSearchOldBinding.inflate(layoutInflater, parent, false)
            }
        ),
        locale = locale
    )
  }

  @Inject
  lateinit var locale: Locale

  @Inject
  lateinit var effectHandler: AllPatientsInFacilityEffectHandler

  @Inject
  lateinit var crashReporter: CrashReporter

  private val viewRenderer = AllPatientsInFacilityUiRenderer(this)

  val uiEvents: Observable<UiEvent>
    get() = Observable.merge(searchResultClicks(), listScrollEvents())
        .compose(ReportAnalyticsEvents())

  private val delegate by unsafeLazy {
    MobiusDelegate.forView(
        events = Observable.never(),
        defaultModel = AllPatientsInFacilityModel.FETCHING_PATIENTS,
        init = AllPatientsInFacilityInit(),
        update = AllPatientsInFacilityUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = viewRenderer::render
    )
  }

  private var binding: ViewAllpatientsinfacilityBinding? = null

  private val patientsList
    get() = binding!!.patientsList

  private val noPatientsContainer
    get() = binding!!.noPatientsContainer

  private val noPatientsLabel
    get() = binding!!.noPatientsLabel

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    binding = ViewAllpatientsinfacilityBinding.bind(this)

    context.injector<Injector>().inject(this)

    setupAllPatientsList()
    setupInitialViewVisibility()
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
    binding = null
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    val viewState = delegate.onRestoreInstanceState(state)
    super.onRestoreInstanceState(viewState)
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

  interface Injector {
    fun inject(target: AllPatientsInFacilityView)
  }
}
