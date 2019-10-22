package org.simple.clinic.addidtopatient.searchresults

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.main.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.newentry.PatientEntryScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.searchresultsview.PatientSearchView
import org.simple.clinic.searchresultsview.RegisterNewPatient
import org.simple.clinic.searchresultsview.SearchPatientWithCriteria
import org.simple.clinic.searchresultsview.SearchResultClicked
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
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class AddIdToPatientSearchResultsScreen(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: AddIdToPatientSearchResultsController

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var identifierDisplayAdapter: IdentifierDisplayAdapter

  private val toolbar by bindView<Toolbar>(R.id.addidtopatientsearchresults_toolbar)
  private val titleTextView by bindView<TextView>(R.id.addidtopatientsearchresults_title)
  private val searchResultsView by bindView<PatientSearchView>(R.id.addidtopatientsearchresults_searchresultsview)
  private val screenKey by unsafeLazy { screenRouter.key<AddIdToPatientSearchResultsScreenKey>(this) }

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)
    setupScreen()

    bindUiToController(
        ui = this,
        events = Observable.mergeArray(
            screenCreates(),
            searchResultClicks(),
            registerNewPatientClicks()
        ),
        controller = controller,
        screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    )
  }

  private fun searchResultClicks(): Observable<UiEvent> {
    return searchResultsView
        .upstreamUiEvents
        .ofType<SearchResultClicked>()
        .map { AddIdToPatientSearchResultClicked(it.patientUuid) }
  }

  private fun registerNewPatientClicks(): Observable<UiEvent> {
    return searchResultsView
        .upstreamUiEvents
        .ofType<RegisterNewPatient>()
        .map { AddIdToPatientSearchResultRegisterNewPatientClicked }
  }

  private fun setupScreen() {
    hideKeyboard()
    toolbar.setNavigationOnClickListener {
      screenRouter.pop()
    }

    val identifierType = identifierDisplayAdapter.typeAsText(screenKey.identifier)
    val identifierValue = identifierDisplayAdapter.valueAsText(screenKey.identifier)

    val identifierTextAppearanceSpan = TextAppearanceWithLetterSpacingSpan(
        context,
        R.style.Clinic_V2_TextAppearance_Body0Left_NumericBold_White100
    )

    titleTextView.text = Truss()
        .append(resources.getString(R.string.addidtopatientsearchresults_add, identifierType))
        .pushSpan(identifierTextAppearanceSpan)
        .append(identifierValue)
        .popSpan()
        .append(resources.getString(R.string.addidtopatientsearchresults_to_patient))
        .build()
  }

  private fun screenCreates(): Observable<UiEvent> {
    val screenKey = screenRouter.key<AddIdToPatientSearchResultsScreenKey>(this)

    searchResultsView
        .downstreamUiEvents
        .onNext(SearchPatientWithCriteria(screenKey.searchCriteria))

    return Observable.just(AddIdToPatientSearchResultsScreenCreated(screenKey.searchCriteria, screenKey.identifier))
  }

  fun openPatientSummaryScreen(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(
        patientUuid,
        OpenIntention.LinkIdWithPatient(screenKey.identifier),
        Instant.now(utcClock)
    ))
  }

  fun openPatientEntryScreen() {
    screenRouter.push(PatientEntryScreenKey())
  }
}
