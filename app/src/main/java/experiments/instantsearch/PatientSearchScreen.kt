package experiments.instantsearch

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.experiment_screen_patient_search.view.*
import kotlinx.android.synthetic.main.view_allpatientsinfacility.view.*
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityListScrolled
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilitySearchResultClicked
import org.simple.clinic.allpatientsinfacility.AllPatientsInFacilityView
import org.simple.clinic.bindUiToController
import org.simple.clinic.newentry.PatientEntryScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.scanid.KeyboardVisibilityDetector
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class PatientSearchScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var controller: PatientSearchScreenController

  @Inject
  lateinit var utcClock: UtcClock

  private val instantSearchResultsAdapter = ItemAdapter(SearchResultItem.DiffCallback())

  private val keyboardVisibilityDetector = KeyboardVisibilityDetector()

  private val allPatientsInFacilityView by unsafeLazy {
    allPatientsView as AllPatientsInFacilityView
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity
        .component
        .experimentsComponentBuilder()
        .build()
        .inject(this)

    backButton.setOnClickListener {
      screenRouter.pop()
    }
    searchQueryEditText.showKeyboard()

    val screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    hideKeyboardWhenAllPatientsListIsScrolled(screenDestroys)
    hideKeyboardWhenInstantSearchResultsListIsScrolled(screenDestroys)

    instantSearchResults.apply {
      setHasFixedSize(true)
      layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
      adapter = instantSearchResultsAdapter
    }

    newPatientButton.setOnClickListener { screenRouter.push(PatientEntryScreenKey()) }

    bindUiToController(
        ui = this,
        events = Observable.merge(
            searchTextChanges(),
            patientClickEvents()
        ),
        controller = controller,
        screenDestroys = screenDestroys
    )
  }

  private fun searchTextChanges(): Observable<UiEvent> {
    return RxTextView
        .textChanges(searchQueryEditText)
        .map(CharSequence::toString)
        .map(::SearchQueryTextChanged)
  }

  private fun patientClickEvents(): Observable<UiEvent> {
    return allPatientsInFacilityView
        .uiEvents
        .ofType<AllPatientsInFacilitySearchResultClicked>()
        .map { PatientItemClicked(it.patientUuid) }
  }

  @Suppress("CheckResult")
  private fun hideKeyboardWhenAllPatientsListIsScrolled(screenDestroys: Observable<ScreenDestroyed>) {
    allPatientsInFacilityView
        .uiEvents
        .ofType<AllPatientsInFacilityListScrolled>()
        .takeUntil(screenDestroys)
        .subscribe { hideKeyboard() }
  }

  @Suppress("CheckResult")
  private fun hideKeyboardWhenInstantSearchResultsListIsScrolled(screenDestroys: Observable<ScreenDestroyed>) {
    RxRecyclerView
        .scrollStateChanges(instantSearchResults)
        .filter { it == RecyclerView.SCROLL_STATE_DRAGGING }
        .takeUntil(screenDestroys)
        .subscribe { hideKeyboard() }
  }

  fun openPatientSummary(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(
        patientUuid = patientUuid,
        intention = OpenIntention.ViewExistingPatient,
        screenCreatedTimestamp = Instant.now(utcClock)
    ))
  }

  fun showInstantSearchResults(results: PatientSearchResults) {
    allPatientsView.visibility = GONE
    instantSearchResults.visibility = VISIBLE
    if (results.hasNoResults) {
      emptyStateView.visibility = VISIBLE
    } else {
      emptyStateView.visibility = GONE
    }
    instantSearchResultsAdapter.submitList(SearchResultItem.from(results))
  }

  fun hideInstantSearchResults() {
    allPatientsView.visibility = VISIBLE
    instantSearchResults.visibility = GONE
    emptyStateView.visibility = GONE
    newPatientContainer.visibility = GONE
    instantSearchResultsAdapter.submitList(emptyList())
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    keyboardVisibilityDetector.registerListener(this) { isKeyboardVisible ->
      if (isKeyboardVisible.not() && (instantSearchResults.visibility == VISIBLE || emptyStateView.visibility == VISIBLE)) {
        newPatientContainer.visibility = VISIBLE
      } else {
        newPatientContainer.visibility = GONE
      }
    }
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    keyboardVisibilityDetector.unregisterListener(this)
  }
}
