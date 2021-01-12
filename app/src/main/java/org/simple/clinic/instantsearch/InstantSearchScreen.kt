package org.simple.clinic.instantsearch

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.recyclerview.scrollStateChanges
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import com.jakewharton.rxbinding3.widget.editorActions
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_instant_search.view.*
import org.simple.clinic.R
import org.simple.clinic.bp.assignbppassport.BpPassportSheet
import org.simple.clinic.databinding.ListPatientSearchBinding
import org.simple.clinic.databinding.ListPatientSearchHeaderBinding
import org.simple.clinic.databinding.ScreenInstantSearchBinding
import org.simple.clinic.di.injector
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.alertchange.AlertFacilityChangeSheet
import org.simple.clinic.facility.alertchange.Continuation
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.newentry.PatientEntryScreenKey
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.extractSuccessful
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class InstantSearchScreen(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs), InstantSearchUi, InstantSearchUiActions {

  companion object {
    private const val BP_PASSPORT_SHEET = 1333
    private const val ALERT_FACILITY_CHANGE = 1444
  }

  @Inject
  lateinit var effectHandlerFactory: InstantSearchEffectHandler.Factory

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var utcClock: UtcClock

  private var binding: ScreenInstantSearchBinding? = null

  private val searchQueryEditText
    get() = binding!!.searchQueryEditText

  private val searchQueryTextInputLayout
    get() = binding!!.searchQueryTextInputLayout

  private val searchResultsView
    get() = binding!!.searchResultsView

  private val newPatientButton
    get() = binding!!.newPatientButton

  private val instantSearchProgressIndicator
    get() = binding!!.instantSearchProgressIndicator

  private val screenKey: InstantSearchScreenKey by unsafeLazy {
    screenRouter.key(this)
  }

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

  private val events by unsafeLazy {
    Observable.mergeArray(
        allPatientsItemClicks(),
        searchItemClicks(),
        searchQueryChanges(),
        registerNewPatientClicks(),
        blankBpPassportResults()
    )
  }

  private val delegate by unsafeLazy {
    val uiRenderer = InstantSearchUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = InstantSearchModel.create(screenKey.additionalIdentifier),
        init = InstantSearchInit(),
        update = InstantSearchUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        modelUpdateListener = uiRenderer::render
    )
  }

  private val detaches = detaches()

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null

    activity.window.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE)

    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) return
    context.injector<Injector>().inject(this)

    activity.window.setSoftInputMode(SOFT_INPUT_ADJUST_NOTHING)

    binding = ScreenInstantSearchBinding.bind(this)

    // When a scanned BP Passport does not result in a match, we bring up a bottom sheet which asks
    // whether this is a new registration or an existing patient. If we show the keyboard in these 
    // cases, the UI is janky since the keyboard pops up and immediately another bottom sheet pops up.
    // This improves the experience by showing the keyboard only if we have arrived here by searching
    // for a patient by the name
    if (screenKey.additionalIdentifier == null) {
      searchQueryEditText.showKeyboard()
    }
    searchQueryTextInputLayout.setStartIconOnClickListener {
      screenRouter.pop()
    }

    searchResultsView.adapter = allPatientsAdapter

    setupAlertResults()
    hideKeyboardOnSearchResultsScroll()
    hideKeyboardOnImeAction()
  }

  override fun showAllPatients(patients: List<PatientSearchResult>, facility: Facility) {
    searchResultsView.visibility = View.VISIBLE
    allPatientsAdapter.submitList(InstantSearchResultsItemType.from(patients, facility))

    searchResultsView.swapAdapter(allPatientsAdapter, false)
    searchResultsView.smoothScrollToPosition(0)
  }

  override fun showPatientsSearchResults(patients: List<PatientSearchResult>, facility: Facility) {
    searchResultsView.visibility = View.VISIBLE
    searchResultsAdapter.submitList(InstantSearchResultsItemType.from(patients, facility))

    searchResultsView.swapAdapter(searchResultsAdapter, false)
    searchResultsView.smoothScrollToPosition(0)
  }

  override fun openPatientSummary(patientId: UUID) {
    screenRouter.push(PatientSummaryScreenKey(
        patientUuid = patientId,
        intention = OpenIntention.ViewExistingPatient,
        screenCreatedTimestamp = Instant.now(utcClock)
    ))
  }

  override fun openLinkIdWithPatientScreen(patientId: UUID, identifier: Identifier) {
    screenRouter.push(PatientSummaryScreenKey(
        patientUuid = patientId,
        intention = OpenIntention.LinkIdWithPatient(identifier),
        screenCreatedTimestamp = Instant.now(utcClock)
    ))
  }

  override fun openBpPassportSheet(identifier: Identifier) {
    val intent = BpPassportSheet.intent(context, identifier)
    activity.startActivityForResult(intent, BP_PASSPORT_SHEET)
  }

  override fun showNoPatientsInFacility(facility: Facility) {
    searchResultsView.visibility = View.GONE
    noPatientsInFacilityContainer.visibility = View.VISIBLE
    noPatientsInFacilityTextView.text = context.getString(R.string.patientsearch_error_no_patients_in_facility_heading, facility.name)

    // In order to get the create button above the keyboard, we
    // update the soft input mode.
    activity.window.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE)
  }

  override fun showNoSearchResults() {
    searchResultsView.visibility = View.GONE
    noSearchResultsContainer.visibility = View.VISIBLE

    // In order to get the create button above the keyboard, we
    // update the soft input mode.
    activity.window.setSoftInputMode(SOFT_INPUT_ADJUST_RESIZE)
  }

  override fun hideNoPatientsInFacility() {
    noPatientsInFacilityContainer.visibility = View.GONE
    activity.window.setSoftInputMode(SOFT_INPUT_ADJUST_NOTHING)
  }

  override fun hideNoSearchResults() {
    noSearchResultsContainer.visibility = View.GONE
    activity.window.setSoftInputMode(SOFT_INPUT_ADJUST_NOTHING)
  }

  override fun openPatientEntryScreen(facility: Facility) {
    activity.startActivityForResult(
        AlertFacilityChangeSheet.intent(context, facility.name, Continuation.ContinueToScreen(PatientEntryScreenKey())),
        ALERT_FACILITY_CHANGE
    )
  }

  override fun showSearchProgress() {
    instantSearchProgressIndicator.visibility = View.VISIBLE
  }

  override fun hideSearchProgress() {
    instantSearchProgressIndicator.visibility = View.GONE
  }

  @SuppressLint("CheckResult")
  private fun setupAlertResults() {
    screenRouter.streamScreenResults()
        .ofType<ActivityResult>()
        .extractSuccessful(ALERT_FACILITY_CHANGE) { intent ->
          AlertFacilityChangeSheet.readContinuationExtra<Continuation.ContinueToScreen>(intent).screenKey
        }
        .takeUntil(detaches)
        .subscribe(screenRouter::push)
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
    return screenRouter
        .streamScreenResults()
        .ofType<ActivityResult>()
        .extractSuccessful(BP_PASSPORT_SHEET, BpPassportSheet.Companion::blankBpPassportResult)
        .map(::BlankBpPassportResultReceived)
  }

  @SuppressLint("CheckResult")
  private fun hideKeyboardOnSearchResultsScroll() {
    searchResultsView
        .scrollStateChanges()
        .filter { it == RecyclerView.SCROLL_STATE_DRAGGING }
        .takeUntil(detaches)
        .subscribe { hideKeyboard() }
  }

  @SuppressLint("CheckResult")
  private fun hideKeyboardOnImeAction() {
    searchQueryEditText
        .editorActions { actionId -> actionId == EditorInfo.IME_ACTION_SEARCH }
        .takeUntil(detaches)
        .subscribe { hideKeyboard() }
  }

  interface Injector {
    fun inject(target: InstantSearchScreen)
  }
}
