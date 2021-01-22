package org.simple.clinic.instantsearch

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.recyclerview.scrollStateChanges
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.editorActions
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_instant_search.*
import org.simple.clinic.R
import org.simple.clinic.bp.assignbppassport.BpPassportSheet
import org.simple.clinic.databinding.ListPatientSearchBinding
import org.simple.clinic.databinding.ListPatientSearchHeaderBinding
import org.simple.clinic.databinding.ScreenInstantSearchBinding
import org.simple.clinic.di.injector
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.alertchange.AlertFacilityChangeSheet
import org.simple.clinic.facility.alertchange.Continuation
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.newentry.PatientEntryScreenKey
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.FullScreenKey
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.extractSuccessful
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class InstantSearchScreen :
    BaseScreen<
        InstantSearchScreenKey,
        ScreenInstantSearchBinding,
        InstantSearchModel,
        InstantSearchEvent,
        InstantSearchEffect,
        InstantSearchUiRenderer>(),
    InstantSearchUi,
    InstantSearchUiActions {

  companion object {
    private const val BP_PASSPORT_SHEET = 1333
    private const val ALERT_FACILITY_CHANGE = 1444
  }

  @Inject
  lateinit var effectHandlerFactory: InstantSearchEffectHandler.Factory

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenResults: ScreenResultBus

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var utcClock: UtcClock

  private val subscriptions = CompositeDisposable()

  private val instantSearchToolbar
    get() = binding.instantSearchToolbar

  private val searchQueryEditText
    get() = binding.searchQueryEditText

  private val searchQueryTextInputLayout
    get() = binding.searchQueryTextInputLayout

  private val searchResultsView
    get() = binding.searchResultsView

  private val newPatientButton
    get() = binding.newPatientButton

  private val instantSearchProgressIndicator
    get() = binding.instantSearchProgressIndicator

  private val allPatientsAdapter = ItemAdapter(
      diffCallback = InstantSearchResultsItemType.DiffCallback(),
      bindings = mapOf(
          R.layout.list_patient_search_header to { layoutInflater, parent ->
            ListPatientSearchHeaderBinding.inflate(layoutInflater, parent, false)
          },
          R.layout.list_patient_search to { layoutInflater, parent ->
            ListPatientSearchBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  private val searchResultsAdapter = ItemAdapter(
      diffCallback = InstantSearchResultsItemType.DiffCallback(),
      bindings = mapOf(
          R.layout.list_patient_search_header to { layoutInflater, parent ->
            ListPatientSearchHeaderBinding.inflate(layoutInflater, parent, false)
          },
          R.layout.list_patient_search to { layoutInflater, parent ->
            ListPatientSearchBinding.inflate(layoutInflater, parent, false)
          }
      )
  )

  override fun defaultModel() = InstantSearchModel.create(screenKey.additionalIdentifier)

  override fun uiRenderer() = InstantSearchUiRenderer(this)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenInstantSearchBinding.inflate(layoutInflater, container, false)

  override fun events() = Observable
      .mergeArray(
          allPatientsItemClicks(),
          searchItemClicks(),
          searchQueryChanges(),
          registerNewPatientClicks(),
          blankBpPassportResults()
      ).cast<InstantSearchEvent>()

  override fun createUpdate() = InstantSearchUpdate()

  override fun createInit() = InstantSearchInit()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    // When a scanned BP Passport does not result in a match, we bring up a bottom sheet which asks
    // whether this is a new registration or an existing patient. If we show the keyboard in these
    // cases, the UI is janky since the keyboard pops up and immediately another bottom sheet pops up.
    // This improves the experience by showing the keyboard only if we have arrived here by searching
    // for a patient by the name
    if (screenKey.additionalIdentifier == null) {
      searchQueryEditText.showKeyboard()
    }
    instantSearchToolbar.setNavigationOnClickListener {
      router.pop()
    }

    searchResultsView.adapter = allPatientsAdapter

    subscriptions.addAll(
        setupAlertResults(),
        hideKeyboardOnSearchResultsScroll(),
        hideKeyboardOnImeAction()
    )
  }

  override fun onDestroyView() {
    super.onDestroyView()
    subscriptions.clear()
  }

  override fun showAllPatients(patients: List<PatientSearchResult>, facility: Facility) {
    searchResultsView.visibility = View.VISIBLE
    allPatientsAdapter.submitList(InstantSearchResultsItemType.from(patients, facility))

    searchResultsView.swapAdapter(allPatientsAdapter, false)
    searchResultsView.scrollToPosition(0)
  }

  override fun showPatientsSearchResults(patients: List<PatientSearchResult>, facility: Facility) {
    searchResultsView.visibility = View.VISIBLE
    searchResultsAdapter.submitList(InstantSearchResultsItemType.from(patients, facility))

    searchResultsView.swapAdapter(searchResultsAdapter, false)
    searchResultsView.scrollToPosition(0)
  }

  override fun openPatientSummary(patientId: UUID) {
    router.push(PatientSummaryScreenKey(
        patientUuid = patientId,
        intention = OpenIntention.ViewExistingPatient,
        screenCreatedTimestamp = Instant.now(utcClock)
    ).wrap())
  }

  override fun openLinkIdWithPatientScreen(patientId: UUID, identifier: Identifier) {
    router.push(PatientSummaryScreenKey(
        patientUuid = patientId,
        intention = OpenIntention.LinkIdWithPatient(identifier),
        screenCreatedTimestamp = Instant.now(utcClock)
    ).wrap())
  }

  override fun openBpPassportSheet(identifier: Identifier) {
    val intent = BpPassportSheet.intent(requireContext(), identifier)
    activity.startActivityForResult(intent, BP_PASSPORT_SHEET)
  }

  override fun showNoPatientsInFacility(facility: Facility) {
    searchResultsView.visibility = View.GONE
    noPatientsInFacilityContainer.visibility = View.VISIBLE
    noPatientsInFacilityTextView.text = getString(R.string.patientsearch_error_no_patients_in_facility_heading, facility.name)
  }

  override fun showNoSearchResults() {
    searchResultsView.visibility = View.GONE
    noSearchResultsContainer.visibility = View.VISIBLE
  }

  override fun hideNoPatientsInFacility() {
    noPatientsInFacilityContainer.visibility = View.GONE
  }

  override fun hideNoSearchResults() {
    noSearchResultsContainer.visibility = View.GONE
  }

  override fun openPatientEntryScreen(facility: Facility) {
    activity.startActivityForResult(
        AlertFacilityChangeSheet.intent(requireContext(), facility.name, Continuation.ContinueToScreen(PatientEntryScreenKey())),
        ALERT_FACILITY_CHANGE
    )
  }

  override fun showSearchProgress() {
    instantSearchProgressIndicator.visibility = View.VISIBLE
  }

  override fun hideSearchProgress() {
    instantSearchProgressIndicator.visibility = View.GONE
  }

  private fun setupAlertResults(): Disposable {
    return screenResults
        .streamResults()
        .ofType<ActivityResult>()
        .extractSuccessful(ALERT_FACILITY_CHANGE) { intent ->
          AlertFacilityChangeSheet.readContinuationExtra<Continuation.ContinueToScreen>(intent).screenKey
        }
        .map(FullScreenKey::wrap)
        .subscribe(router::push)
  }

  private fun allPatientsItemClicks(): Observable<UiEvent> {
    return allPatientsAdapter
        .itemEvents
        .ofType<InstantSearchResultsItemType.Event.ResultClicked>()
        .map { SearchResultClicked(it.patientUuid) }
  }

  private fun searchItemClicks(): Observable<UiEvent> {
    return searchResultsAdapter
        .itemEvents
        .ofType<InstantSearchResultsItemType.Event.ResultClicked>()
        .map { SearchResultClicked(it.patientUuid) }
  }

  private fun searchQueryChanges(): Observable<UiEvent> {
    return searchQueryEditText
        .textChanges()
        .debounce(500, TimeUnit.MILLISECONDS)
        .map { SearchQueryChanged(it.toString()) }
  }

  private fun registerNewPatientClicks(): Observable<UiEvent> {
    return newPatientButton
        .clicks()
        .map { RegisterNewPatientClicked }
  }

  private fun blankBpPassportResults(): Observable<UiEvent> {
    return screenResults
        .streamResults()
        .ofType<ActivityResult>()
        .extractSuccessful(BP_PASSPORT_SHEET, BpPassportSheet.Companion::blankBpPassportResult)
        .map(::BlankBpPassportResultReceived)
  }

  private fun hideKeyboardOnSearchResultsScroll(): Disposable {
    return searchResultsView
        .scrollStateChanges()
        .filter { it == RecyclerView.SCROLL_STATE_DRAGGING }
        .subscribe { binding.root.hideKeyboard() }
  }

  private fun hideKeyboardOnImeAction(): Disposable {
    return searchQueryEditText
        .editorActions { actionId -> actionId == EditorInfo.IME_ACTION_SEARCH }
        .subscribe { binding.root.hideKeyboard() }
  }

  interface Injector {
    fun inject(target: InstantSearchScreen)
  }
}
