package org.simple.clinic.search.results

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.screen_patient_search_results.view.*
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.bindUiToController
import org.simple.clinic.di.injector
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.alertchange.AlertFacilityChangeSheet
import org.simple.clinic.facility.alertchange.Continuation.ContinueToScreen
import org.simple.clinic.mobius.MobiusDelegate
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
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.ScreenDestroyed
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class PatientSearchResultsScreen(
    context: Context,
    attrs: AttributeSet
) : RelativeLayout(context, attrs), PatientSearchResultsUi {

  @Inject
  lateinit var screenRouter: ScreenRouter

  @Inject
  lateinit var controllerInjectionFactory: PatientSearchResultsController.InjectionFactory

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var effectHandlerInjectionFactory: PatientSearchResultsEffectHandler.InjectionFactory

  private val screenKey by unsafeLazy { screenRouter.key<PatientSearchResultsScreenKey>(this) }

  private val events by unsafeLazy {
    Observable
        .merge(
            screenCreates(),
            searchResultClicks(),
            registerNewPatientClicks()
        )
        .compose(ReportAnalyticsEvents())
        .share()
  }

  private val delegate by unsafeLazy {
    val uiRenderer = PatientSearchResultsUiRenderer(this)

    MobiusDelegate.forView(
        events = events.ofType(),
        defaultModel = PatientSearchResultsModel.create(),
        update = PatientSearchResultsUpdate(),
        effectHandler = effectHandlerInjectionFactory.create(this).build(),
        init = PatientSearchResultsInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  @SuppressLint("CheckResult")
  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    context.injector<Injector>().inject(this)
    setupScreen()

    val screenDestroys = detaches().map { ScreenDestroyed() }

    bindUiToController(
        ui = this,
        events = events,
        controller = controllerInjectionFactory.create(screenKey.criteria),
        screenDestroys = screenDestroys
    )

    setupAlertResults(screenDestroys)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    delegate.start()
  }

  override fun onDetachedFromWindow() {
    delegate.stop()
    super.onDetachedFromWindow()
  }

  override fun onSaveInstanceState(): Parcelable? {
    return delegate.onSaveInstanceState(super.onSaveInstanceState())
  }

  override fun onRestoreInstanceState(state: Parcelable?) {
    super.onRestoreInstanceState(delegate.onRestoreInstanceState(state))
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
    return Observable.just(PatientSearchResultsScreenCreated())
  }

  override fun openPatientSummaryScreen(patientUuid: UUID) {
    screenRouter.push(PatientSummaryScreenKey(patientUuid, OpenIntention.ViewExistingPatient, Instant.now(utcClock)))
  }

  override fun openLinkIdWithPatientScreen(patientUuid: UUID, identifier: Identifier) {
    screenRouter.push(PatientSummaryScreenKey(patientUuid, OpenIntention.LinkIdWithPatient(identifier), Instant.now(utcClock)))
  }

  override fun openPatientEntryScreen(facility: Facility) {
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
