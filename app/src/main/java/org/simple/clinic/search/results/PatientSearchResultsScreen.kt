package org.simple.clinic.search.results

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_patient_search_results.view.*
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.alertchange.AlertFacilityChangeSheet
import org.simple.clinic.facility.alertchange.Continuation.ContinueToScreen
import org.simple.clinic.newentry.PatientEntryScreenKey
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.PatientSearchCriteria.Name
import org.simple.clinic.patient.PatientSearchCriteria.PhoneNumber
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.router.screen.ActivityResult
import org.simple.clinic.router.screen.ScreenRouter
import org.simple.clinic.searchresultsview.RegisterNewPatient
import org.simple.clinic.searchresultsview.SearchPatientWithCriteria
import org.simple.clinic.searchresultsview.SearchResultClicked
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.extractSuccessful
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class PatientSearchResultsScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controller: PatientSearchResultsController

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var activity: AppCompatActivity

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)
    setupScreen()

    val screenDestroys = RxView.detaches(this).map { ScreenDestroyed() }
    bindUiToController(
        ui = this,
        events = Observable.merge(
            screenCreates(),
            searchResultClicks(),
            registerNewPatientClicks()
        ),
        controller = controller,
        screenDestroys = screenDestroys
    )

    setupAlertResults(screenDestroys)
  }

  @SuppressLint("CheckResult")
  private fun setupAlertResults(screenDestroys: Observable<ScreenDestroyed>) {
    screenRouter.streamScreenResults()
        .ofType<ActivityResult>()
        .extractSuccessful(ALERT_FACILITY_CHANGE) { intent ->
          AlertFacilityChangeSheet.readContinuationExtra<ContinueToScreen>(intent).screenKey
        }
        .takeUntil(screenDestroys)
        .subscribe(screenRouter::push)
  }

  private fun searchResultClicks(): Observable<UiEvent> {
    return searchResultsView
        .upstreamUiEvents
        .ofType<SearchResultClicked>()
        .map { PatientSearchResultClicked(it.patientUuid) }
  }

  private fun registerNewPatientClicks(): Observable<UiEvent> {
    return searchResultsView
        .upstreamUiEvents
        .ofType<RegisterNewPatient>()
        .map { PatientSearchResultRegisterNewPatient(it.criteria) }
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
    toolbar.title = generateToolbarTitleForCriteria(screenKey.criteria)
  }

  private fun generateToolbarTitleForCriteria(patientSearchCriteria: PatientSearchCriteria): CharSequence {
    return when (patientSearchCriteria) {
      is Name -> patientSearchCriteria.patientName
      is PhoneNumber -> patientSearchCriteria.phoneNumber
    }
  }

  private fun screenCreates(): Observable<UiEvent> {
    val screenKey = screenRouter.key<PatientSearchResultsScreenKey>(this)
    searchResultsView
        .downstreamUiEvents
        .onNext(SearchPatientWithCriteria(screenKey.criteria))
    return Observable.just(PatientSearchResultsScreenCreated(screenKey))
  }

  fun openPatientSummaryScreen(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(patientUuid, OpenIntention.ViewExistingPatient, Instant.now(utcClock)))
  }

  fun openLinkIdWithPatientScreen(patientUuid: UUID, identifier: Identifier) {
    screenRouter.push(PatientSummaryScreenKey(patientUuid, OpenIntention.LinkIdWithPatient(identifier), Instant.now(utcClock)))
  }

  fun openPatientEntryScreen(facility: Facility) {
    activity.startActivityForResult(
        AlertFacilityChangeSheet.intent(context, facility.name, ContinueToScreen(PatientEntryScreenKey())),
        ALERT_FACILITY_CHANGE
    )
  }

  interface Injector {
    fun inject(target: PatientSearchResultsScreen)
  }

  companion object {
    private const val ALERT_FACILITY_CHANGE = 1122
  }
}
