package org.simple.clinic.instantsearch

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
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
import io.reactivex.subjects.PublishSubject
import kotlinx.parcelize.Parcelize
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ListPatientSearchBinding
import org.simple.clinic.databinding.ListPatientSearchHeaderBinding
import org.simple.clinic.databinding.ScreenInstantSearchBinding
import org.simple.clinic.di.injector
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.alertchange.AlertFacilityChangeSheet
import org.simple.clinic.facility.alertchange.Continuation
import org.simple.clinic.feature.Feature.InstantSearchByPatientIdentifier
import org.simple.clinic.feature.Feature.InstantSearchQrCode
import org.simple.clinic.feature.Features
import org.simple.clinic.navigation.v2.ExpectsResult
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenResult
import org.simple.clinic.navigation.v2.Succeeded
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.newentry.PatientEntryScreenKey
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.scanid.OpenedFrom
import org.simple.clinic.scanid.ScanSimpleIdScreenKey
import org.simple.clinic.scanid.scannedqrcode.NationalHealthIDErrorDialog
import org.simple.clinic.scanid.scannedqrcode.ScannedQrCodeSheet
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.RequestPermissions
import org.simple.clinic.util.RuntimePermissions
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import org.simple.clinic.widgets.visibleOrGone
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class InstantSearchScreen :
    BaseScreen<
        InstantSearchScreenKey,
        ScreenInstantSearchBinding,
        InstantSearchModel,
        InstantSearchEvent,
        InstantSearchEffect>(),
    InstantSearchUi,
    InstantSearchUiActions,
    ExpectsResult {

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

  @Inject
  lateinit var features: Features

  @Inject
  lateinit var runtimePermissions: RuntimePermissions

  @Inject
  @Named("date_for_user_input")
  lateinit var dateTimeFormatter: DateTimeFormatter


  private val subscriptions = CompositeDisposable()

  private val instantSearchToolbar
    get() = binding.instantSearchToolbar

  private val searchQueryEditText
    get() = binding.searchQueryEditText

  private val searchResultsView
    get() = binding.searchResultsView

  private val newPatientButton
    get() = binding.newPatientButton

  private val instantSearchProgressIndicator
    get() = binding.instantSearchProgressIndicator

  private val noPatientsInFacilityContainer
    get() = binding.noPatientsInFacilityContainer

  private val noPatientsInFacilityTextView
    get() = binding.noPatientsInFacilityTextView

  private val noSearchResultsContainer
    get() = binding.noSearchResultsContainer

  private val qrCodeScannerButton
    get() = binding.qrCodeScannerButton

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

  private val blankScannedQrCodeResults = PublishSubject.create<UiEvent>()

  override fun defaultModel() = InstantSearchModel.create(screenKey.additionalIdentifier, screenKey.patientPrefillInfo, screenKey.initialSearchQuery)

  override fun uiRenderer() = InstantSearchUiRenderer(this)

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenInstantSearchBinding.inflate(layoutInflater, container, false)

  override fun events() = Observable
      .mergeArray(
          allPatientsItemClicks(),
          searchItemClicks(),
          searchQueryChanges(),
          registerNewPatientClicks(),
          blankScannedQrCodeResults,
          openQrCodeScannerClicks()
      )
      .compose(RequestPermissions(runtimePermissions, screenResults.streamResults().ofType()))
      .compose(ReportAnalyticsEvents())
      .cast<InstantSearchEvent>()

  override fun createUpdate() = InstantSearchUpdate(features.isEnabled(InstantSearchByPatientIdentifier), dateTimeFormatter)

  override fun createInit() = InstantSearchInit()

  override fun createEffectHandler() = effectHandlerFactory.create(this).build()

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    instantSearchToolbar.setNavigationOnClickListener {
      router.pop()
    }

    searchQueryEditText.setText(screenKey.initialSearchQuery.orEmpty())

    qrCodeScannerButton.visibleOrGone(features.isEnabled(InstantSearchQrCode))

    searchResultsView.adapter = allPatientsAdapter

    subscriptions.addAll(
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
    allPatientsAdapter.submitList(InstantSearchResultsItemType.from(patients, facility, searchQuery = null))

    searchResultsView.swapAdapter(allPatientsAdapter, false)
    searchResultsView.scrollToPosition(0)
  }

  override fun showPatientsSearchResults(
      patients: List<PatientSearchResult>,
      facility: Facility,
      searchQuery: String
  ) {
    searchResultsView.visibility = View.VISIBLE
    searchResultsAdapter.submitList(InstantSearchResultsItemType.from(patients, facility, searchQuery))

    searchResultsView.swapAdapter(searchResultsAdapter, false)
    searchResultsView.scrollToPosition(0)
  }

  override fun openPatientSummary(patientId: UUID) {
    router.push(PatientSummaryScreenKey(
        patientUuid = patientId,
        intention = OpenIntention.ViewExistingPatient,
        screenCreatedTimestamp = Instant.now(utcClock)
    ))
  }

  override fun openLinkIdWithPatientScreen(patientId: UUID, identifier: Identifier) {
    router.push(PatientSummaryScreenKey(
        patientUuid = patientId,
        intention = OpenIntention.LinkIdWithPatient(identifier),
        screenCreatedTimestamp = Instant.now(utcClock)
    ))
  }

  override fun openScannedQrCodeSheet(identifier: Identifier) {
    router.pushExpectingResult(BlankScannedQrCode, ScannedQrCodeSheet.Key(identifier))
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
    router.push(AlertFacilityChangeSheet.Key(
        currentFacilityName = facility.name,
        continuation = Continuation.ContinueToScreen_Old(PatientEntryScreenKey())
    ))
  }

  override fun showSearchProgress() {
    instantSearchProgressIndicator.visibility = View.VISIBLE
  }

  override fun hideSearchProgress() {
    instantSearchProgressIndicator.visibility = View.GONE
  }

  override fun showKeyboard() {
    searchQueryEditText.showKeyboard()
  }

  override fun openQrCodeScanner() {
    router.push(ScanSimpleIdScreenKey(OpenedFrom.InstantSearchScreen))
  }

  override fun onScreenResult(requestType: Parcelable, result: ScreenResult) {
    if (requestType == BlankScannedQrCode && result is Succeeded) {
      val scannedQrCodeResult = ScannedQrCodeSheet.blankScannedQrCodeResult(result)
      blankScannedQrCodeResults.onNext(BlankScannedQrCodeResultReceived(scannedQrCodeResult))
    }
  }

  override fun showNHIDErrorDialog() {
    NationalHealthIDErrorDialog.show(activity.supportFragmentManager)
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
        .skipInitialValue()
        .debounce(500, TimeUnit.MILLISECONDS)
        .map { searchQuery ->
          val trimmedSearchQuery = searchQuery
              .trim()
              .toString()

          SearchQueryChanged(trimmedSearchQuery)
        }
  }

  private fun registerNewPatientClicks(): Observable<UiEvent> {
    return newPatientButton
        .clicks()
        .map { RegisterNewPatientClicked }
  }

  private fun openQrCodeScannerClicks(): Observable<UiEvent> {
    return qrCodeScannerButton
        .clicks()
        .map { OpenQrCodeScannerClicked() }
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

  @Parcelize
  private object BlankScannedQrCode : Parcelable
}
