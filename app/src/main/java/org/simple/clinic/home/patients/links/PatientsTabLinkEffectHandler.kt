package org.simple.clinic.home.patients.links

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.facility.Facility
import org.simple.clinic.util.scheduler.SchedulersProvider

class PatientsTabLinkEffectHandler @AssistedInject constructor(
    private val currentFacility: Observable<Facility>,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: PatientsTabLinkUiActions
) {
  @AssistedFactory
  interface Factory {
    fun create(uiActions: PatientsTabLinkUiActions): PatientsTabLinkEffectHandler
  }

  fun build(): ObservableTransformer<PatientsTabLinkEffect, PatientsTabLinkEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientsTabLinkEffect, PatientsTabLinkEvent>()
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility(schedulersProvider.io()))
        .addAction(OpenMonthlyScreeningReportsListScreen::class.java, { uiActions.openMonthlyScreeningReports() }, schedulersProvider.ui())
        .addAction(OpenPatientLineListDownloadDialog::class.java, { uiActions.openPatientLineListDownloadDialog() }, schedulersProvider.ui())
        .build()
  }

  private fun loadCurrentFacility(scheduler: Scheduler):
      ObservableTransformer<LoadCurrentFacility, PatientsTabLinkEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .switchMap { currentFacility }
          .map(::CurrentFacilityLoaded)
    }
  }
}
