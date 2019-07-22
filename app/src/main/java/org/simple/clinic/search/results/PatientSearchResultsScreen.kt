package org.simple.clinic.search.results

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.bindUiToController
import org.simple.clinic.newentry.PatientEntryScreenKey
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.searchresultsview.PatientSearchView
import org.simple.clinic.searchresultsview.RegisterNewPatient
import org.simple.clinic.searchresultsview.SearchPatientInput
import org.simple.clinic.searchresultsview.SearchPatientWithInput
import org.simple.clinic.searchresultsview.SearchResultClicked
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

class PatientSearchResultsScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientSearchResultsController

  @Inject
  lateinit var utcClock: UtcClock

  private val toolbar by bindView<Toolbar>(R.id.patientsearchresults_toolbar)
  private val searchResultsView by bindView<PatientSearchView>(R.id.patientsearchresults_searchresultsview)

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
    TheActivity.component.inject(this)
    setupScreen()

    bindUiToController(
        ui = this,
        events = Observable.merge(
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
        .map { PatientSearchResultClicked(it.searchResult) }
  }

  private fun registerNewPatientClicks(): Observable<UiEvent> {
    return searchResultsView
        .upstreamUiEvents
        .ofType<RegisterNewPatient>()
        .map { PatientSearchResultRegisterNewPatient(extractPatientName(it.searchPatientInput)) }
  }

  private fun extractPatientName(searchPatientInput: SearchPatientInput): String {
    return when (searchPatientInput) {
      is SearchPatientInput.Name -> searchPatientInput.searchText
      is SearchPatientInput.PhoneNumber -> TODO("not yet implemented")
    }
  }

  private fun setupScreen() {
    hideKeyboard()
    toolbar.setNavigationOnClickListener {
      screenRouter.pop()
    }
    toolbar.setOnClickListener {
      screenRouter.pop()
    }

    val screenKey = screenRouter.key<PatientSearchResultsScreenKey>(this)
    toolbar.title = screenKey.fullName
  }

  private fun screenCreates(): Observable<UiEvent> {
    val screenKey = screenRouter.key<PatientSearchResultsScreenKey>(this)
    searchResultsView
        .downstreamUiEvents
        .onNext(SearchPatientWithInput(SearchPatientInput.Name(screenKey.fullName)))
    return Observable.just(PatientSearchResultsScreenCreated(screenKey))
  }

  fun openPatientSummaryScreen(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(patientUuid, OpenIntention.ViewExistingPatient, Instant.now(utcClock)))
  }

  fun openPatientEntryScreen() {
    screenRouter.push(PatientEntryScreenKey())
  }
}
