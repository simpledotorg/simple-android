package org.simple.clinic.search.results

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.rxkotlin.cast
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.databinding.ScreenPatientSearchResultsBinding
import org.simple.clinic.di.injector
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.alertchange.AlertFacilityChangeSheet
import org.simple.clinic.facility.alertchange.Continuation.ContinueToScreen
import org.simple.clinic.mobius.MobiusDelegate
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.compat.wrap
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import org.simple.clinic.newentry.PatientEntryScreenKey
import org.simple.clinic.patient.PatientSearchCriteria
import org.simple.clinic.patient.PatientSearchCriteria.Name
import org.simple.clinic.patient.PatientSearchCriteria.PhoneNumber
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.router.ScreenResultBus
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenKey
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.unsafeLazy
import org.simple.clinic.widgets.UiEvent
import org.simple.clinic.widgets.hideKeyboard
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class PatientSearchResultsScreen :
    BaseScreen<
        PatientSearchResultsScreenKey,
        ScreenPatientSearchResultsBinding,
        PatientSearchResultsModel,
        PatientSearchResultsEvent,
        PatientSearchResultsEffect>(),
    PatientSearchResultsUi,
    PatientSearchResultsUiActions {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var screenResults: ScreenResultBus

  @Inject
  lateinit var utcClock: UtcClock

  @Inject
  lateinit var activity: AppCompatActivity

  @Inject
  lateinit var effectHandlerInjectionFactory: PatientSearchResultsEffectHandler.InjectionFactory

  private val searchResultsView
    get() = binding.searchResultsView

  private val toolbar
    get() = binding.toolbar

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) =
      ScreenPatientSearchResultsBinding.inflate(layoutInflater, container, false)

  override fun defaultModel() = PatientSearchResultsModel.create(screenKey.criteria)

  override fun uiRenderer() = PatientSearchResultsUiRenderer(this)

  override fun events() = Observable
      .merge(
          searchResultClicks(),
          registerNewPatientClicks()
      )
      .compose(ReportAnalyticsEvents())
      .cast<PatientSearchResultsEvent>()

  override fun createUpdate() = PatientSearchResultsUpdate()

  override fun createInit() = PatientSearchResultsInit()

  override fun createEffectHandler() = effectHandlerInjectionFactory.create(this).build()

  private val events by unsafeLazy {
    Observable
        .merge(
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
        defaultModel = PatientSearchResultsModel.create(screenKey.criteria),
        update = PatientSearchResultsUpdate(),
        effectHandler = effectHandlerInjectionFactory.create(this).build(),
        init = PatientSearchResultsInit(),
        modelUpdateListener = uiRenderer::render
    )
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupScreen()
    searchResultsView.searchWithCriteria(screenKey.criteria)
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

  private fun searchResultClicks(): Observable<UiEvent> {
    return Observable.create { emitter ->
      emitter.setCancellable { searchResultsView.searchResultClicked = null }

      searchResultsView.searchResultClicked = { emitter.onNext(PatientSearchResultClicked(it)) }
    }
  }

  private fun registerNewPatientClicks(): Observable<UiEvent> {
    return Observable.create { emitter ->
      emitter.setCancellable { searchResultsView.registerNewPatientClicked = null }

      searchResultsView.registerNewPatientClicked = { emitter.onNext(PatientSearchResultRegisterNewPatient(screenKey.criteria)) }
    }
  }

  private fun setupScreen() {
    binding.root.hideKeyboard()
    toolbar.setNavigationOnClickListener {
      router.pop()
    }
    toolbar.setOnClickListener {
      router.pop()
    }

    toolbar.title = generateToolbarTitleForCriteria(screenKey.criteria)
  }

  private fun generateToolbarTitleForCriteria(patientSearchCriteria: PatientSearchCriteria): CharSequence {
    return when (patientSearchCriteria) {
      is Name -> patientSearchCriteria.patientName
      is PhoneNumber -> patientSearchCriteria.phoneNumber
    }
  }

  override fun openPatientSummaryScreen(patientUuid: UUID) {
    router.push(PatientSummaryScreenKey(patientUuid, OpenIntention.ViewExistingPatient, Instant.now(utcClock)).wrap())
  }

  override fun openLinkIdWithPatientScreen(patientUuid: UUID, identifier: Identifier) {
    router.push(PatientSummaryScreenKey(patientUuid, OpenIntention.LinkIdWithPatient(identifier), Instant.now(utcClock)).wrap())
  }

  override fun openPatientEntryScreen(facility: Facility) {
    router.push(AlertFacilityChangeSheet.Key(
        currentFacilityName = facility.name,
        continuation = ContinueToScreen(PatientEntryScreenKey())
    ))
  }

  interface Injector {
    fun inject(target: PatientSearchResultsScreen)
  }
}
