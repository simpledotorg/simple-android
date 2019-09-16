package org.simple.clinic.addidtopatient.searchforpatient

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_addidtopatientsearch.view.*
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.addidtopatient.searchresults.AddIdToPatientSearchResultsScreenKey
import org.simple.clinic.allpatientsinfacility.migration.AllPatientsInFacilityViewMigration
import org.simple.clinic.allpatientsinfacility.migration.ExposesUiEvents
import org.simple.clinic.allpatientsinfacility_old.AllPatientsInFacilityListScrolled
import org.simple.clinic.allpatientsinfacility_old.AllPatientsInFacilitySearchResultClicked
import org.simple.clinic.bindUiToController
import org.simple.clinic.mobius.migration.MobiusMigrationConfig
import org.simple.clinic.mobius.migration.RevertToCommit
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.text.style.TextAppearanceWithLetterSpacingSpan
import org.simple.clinic.util.Truss
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.identifierdisplay.IdentifierDisplayAdapter
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.simple.clinic.widgets.showKeyboard
import org.threeten.bp.Instant
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@AllPatientsInFacilityViewMigration(
    notes = "Removes toggling mechanism, just make sure the reverted layouts do not contain layouts suffixed with `_old`."
)
@RevertToCommit(
    withMessage = "Make AddIdToPatientSearchScreen to use AllPatientsInFacilityView (Mobius)",
    afterCommitId = "4bd5c87cf567ec64184622a0a3699ce93675e28d"
)
class AddIdToPatientSearchScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var activity: TheActivity

  @Inject
  lateinit var controller: AddIdToPatientSearchScreenController

  @Inject
  lateinit var identifierDisplayAdapter: IdentifierDisplayAdapter

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var mobiusMigrationConfig: MobiusMigrationConfig

  private val screenKey by unsafeLazy {
    screenRouter.key<AddIdToPatientSearchScreenKey>(this)
  }

  private val allPatientsInFacilityView by unsafeLazy {
    allPatientsViewStub.layoutResource = if (mobiusMigrationConfig.useAllPatientsInFacilityView) {
      Timber.d("Using AllPatientsInFacilityView (Mobius)")
      R.layout.view_allpatientsinfacility
    } else {
      Timber.d("Using AllPatientsInFacilityView (OG Architecture)")
      R.layout.view_allpatientsinfacility_old
    }

    allPatientsViewStub.inflate() as ExposesUiEvents
  }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)

    toolBar.setNavigationOnClickListener {
      screenRouter.pop()
    }
    displayScreenTitle()
    searchQueryEditText.showKeyboard()

    val screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    hideKeyboardWhenAllPatientsListIsScrolled(screenDestroys)

    bindUiToController(
        ui = this,
        events = Observable.merge(
            searchQueryChanges(),
            searchClicks(),
            patientClickEvents()
        ),
        controller = controller,
        screenDestroys = screenDestroys
    )
  }

  private fun displayScreenTitle() {
    val identifierType = identifierDisplayAdapter.typeAsText(screenKey.identifier)
    val identifierValue = identifierDisplayAdapter.valueAsText(screenKey.identifier)

    val identifierTextAppearanceSpan = TextAppearanceWithLetterSpacingSpan(
        context,
        R.style.Clinic_V2_TextAppearance_Body0Left_NumericBold_White100
    )

    titleTextView.text = Truss()
        .append(resources.getString(R.string.addidtopatientsearch_add, identifierType))
        .pushSpan(identifierTextAppearanceSpan)
        .append(identifierValue)
        .popSpan()
        .append(resources.getString(R.string.addidtopatientsearch_to_patient))
        .build()
  }

  private fun searchQueryChanges(): Observable<UiEvent> {
    return RxTextView
        .textChanges(searchQueryEditText)
        .map(CharSequence::toString)
        .map(::SearchQueryTextChanged)
  }

  private fun searchClicks(): Observable<SearchClicked> {
    val imeSearchClicks = RxTextView
        .editorActionEvents(searchQueryEditText)
        .filter { it.actionId() == EditorInfo.IME_ACTION_SEARCH }

    return RxView
        .clicks(searchButtonFrame.button)
        .mergeWith(imeSearchClicks)
        .map { SearchClicked }
  }

  private fun patientClickEvents(): Observable<UiEvent> {
    return allPatientsInFacilityView
        .uiEvents
        .ofType<AllPatientsInFacilitySearchResultClicked>()
        .map { it.patientUuid }
        .map(::PatientItemClicked)
  }

  @Suppress("CheckResult")
  private fun hideKeyboardWhenAllPatientsListIsScrolled(screenDestroys: Observable<ScreenDestroyed>) {
    allPatientsInFacilityView
        .uiEvents
        .ofType<AllPatientsInFacilityListScrolled>()
        .takeUntil(screenDestroys)
        .subscribe { enterPatientNameInputContainer.hideKeyboard() }
  }

  fun openAddIdToPatientSearchResultsScreen(criteria: PatientSearchCriteria) {
    screenRouter.push(AddIdToPatientSearchResultsScreenKey(
        searchCriteria = criteria,
        identifier = screenKey.identifier
    ))
  }

  fun setEmptySearchQueryErrorVisible(visible: Boolean) {
    searchQueryEditText.error = if (visible) {
      resources.getString(R.string.addidtopatientsearch_error_empty_fullname)
    } else null
  }

  fun openPatientSummary(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(
        patientUuid = patientUuid,
        intention = OpenIntention.LinkIdWithPatient(screenKey.identifier),
        screenCreatedTimestamp = Instant.now(utcClock)
    ))
  }

  fun showAllPatientsInFacility() {
    (allPatientsInFacilityView as View).visibility = View.VISIBLE
  }

  fun hideAllPatientsInFacility() {
    (allPatientsInFacilityView as View).visibility = View.GONE
  }

  fun showSearchButton() {
    searchButtonFrame.visibility = View.VISIBLE
  }

  fun hideSearchButton() {
    searchButtonFrame.visibility = View.GONE
  }
}
