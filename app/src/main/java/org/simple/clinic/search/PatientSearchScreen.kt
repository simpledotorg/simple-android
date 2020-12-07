package org.simple.clinic.search

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import com.jakewharton.rxbinding3.widget.editorActionEvents
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.R
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityListScrolled
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilitySearchResultClicked
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityView
import org.simple.clinic.databinding.ScreenPatientSearchBinding
import org.simple.clinic.di.injector
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.search.results.PatientSearchResultsScreenKey
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class PatientSearchScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), PatientSearchUi, PatientSearchUiActions {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var effectHandlerFactory: PatientSearchEffectHandler.Factory

  private var binding: ScreenPatientSearchBinding? = null

  private val allPatientsInFacilityView
    get() = binding!!.allPatientsInFacilityView.rootLayout

  private val searchQueryTextInputLayout
    get() = binding!!.searchQueryTextInputLayout

  private val searchQueryEditText
    get() = binding!!.searchQueryEditText

  private val searchButtonFrame
    get() = binding!!.searchButtonFrame

  private val allPatientsInFacility: AllPatientsInFacilityView by unsafeLazy {
    allPatientsInFacilityView
  }

  private val screenKey by unsafeLazy {
    screenRouter.key<PatientSearchScreenKey>(this)
  }

  private val events by unsafeLazy {
    Observable
        .merge(
            searchTextChanges(),
            searchClicks(),
            patientClickEvents()
        )
        .compose(ReportAnalyticsEvents())
  }

  private val delegate by unsafeLazy {
    val uiRenderer = PatientSearchUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = PatientSearchModel.create(screenKey.additionalIdentifier),
        update = PatientSearchUpdate(),
        effectHandler = effectHandlerFactory.create(this).build(),
        init = PatientSearchInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    binding = ScreenPatientSearchBinding.bind(this)

    context.injector<Injector>().inject(this)

    searchQueryTextInputLayout.setStartIconOnClickListener {
      screenRouter.pop()
    }
    searchQueryEditText.showKeyboard()

    val screenDestroys = detaches().map { ScreenDestroyed() }
    hideKeyboardWhenAllPatientsListIsScrolled(screenDestroys)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    binding = null
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
  }

  private fun searchTextChanges(): Observable<UiEvent> {
    return searchQueryEditText
        .textChanges()
        .map(CharSequence::toString)
        .map(::SearchQueryTextChanged)
  }

  private fun searchClicks(): Observable<SearchClicked> {
    val imeSearchClicks = searchQueryEditText
        .editorActionEvents()
        .filter { it.actionId == EditorInfo.IME_ACTION_SEARCH }
        .map { SearchClicked() }

    val searchClicksFromButton = searchButtonFrame
        .button
        .clicks()
        .map { SearchClicked() }

    return searchClicksFromButton.mergeWith(imeSearchClicks)
  }

  private fun patientClickEvents(): Observable<UiEvent> {
    return allPatientsInFacility
        .uiEvents
        .ofType<AllPatientsInFacilitySearchResultClicked>()
        .map { PatientItemClicked(it.patientUuid) }
  }

  @Suppress("CheckResult")
  private fun hideKeyboardWhenAllPatientsListIsScrolled(screenDestroys: Observable<ScreenDestroyed>) {
    allPatientsInFacility
        .uiEvents
        .ofType<AllPatientsInFacilityListScrolled>()
        .takeUntil(screenDestroys)
        .subscribe { hideKeyboard() }
  }

  override fun openSearchResultsScreen(criteria: PatientSearchCriteria) {
    screenRouter.push(PatientSearchResultsScreenKey(criteria))
  }

  override fun setEmptyTextFieldErrorVisible(visible: Boolean) {
    searchQueryTextInputLayout.error = if (visible) {
      resources.getString(R.string.patientsearch_error_empty_fullname)
    } else null
  }

  override fun openPatientSummary(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(
        patientUuid = patientUuid,
        intention = OpenIntention.ViewExistingPatient,
        screenCreatedTimestamp = Instant.now(utcClock)
    ))
  }

  override fun showAllPatientsInFacility() {
    allPatientsInFacility.visibility = View.VISIBLE
  }

  override fun hideAllPatientsInFacility() {
    allPatientsInFacility.visibility = View.GONE
  }

  override fun showSearchButton() {
    searchButtonFrame.visibility = View.VISIBLE
  }

  override fun hideSearchButton() {
    searchButtonFrame.visibility = View.GONE
  }

  interface Injector {
    fun inject(target: PatientSearchScreen)
  }
}
