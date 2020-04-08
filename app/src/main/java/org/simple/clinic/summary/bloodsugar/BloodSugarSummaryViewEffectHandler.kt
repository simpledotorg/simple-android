package org.simple.clinic.summary.bloodsugar

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider

class BloodSugarSummaryViewEffectHandler @AssistedInject constructor(
    private val bloodSugarRepository: BloodSugarRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: UiActions,
    private val config: BloodSugarSummaryConfig,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): BloodSugarSummaryViewEffectHandler
  }

  fun build(): ObservableTransformer<BloodSugarSummaryViewEffect, BloodSugarSummaryViewEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodSugarSummaryViewEffect, BloodSugarSummaryViewEvent>()
        .addTransformer(FetchBloodSugarSummary::class.java, fetchBloodSugarMeasurements(bloodSugarRepository, schedulersProvider.ui()))
        .addTransformer(FetchBloodSugarCount::class.java, fetchBloodSugarMeasurementsCount(schedulersProvider.io()))
        .addTransformer(FetchCurrentFacility::class.java, fetchCurrentFacility(schedulersProvider.io()))
        .addConsumer(OpenBloodSugarTypeSelector::class.java, { uiActions.showBloodSugarTypeSelector(it.currentFacility) }, schedulersProvider.ui())
        .addConsumer(ShowBloodSugarHistoryScreen::class.java, { uiActions.showBloodSugarHistoryScreen(it.patientUuid) }, schedulersProvider.ui())
        .addConsumer(OpenBloodSugarUpdateSheet::class.java, { uiActions.openBloodSugarUpdateSheet(it.measurement.uuid, it.measurement.reading.type) }, schedulersProvider.ui())
        .build()
  }

  private fun fetchCurrentFacility(io: Scheduler): ObservableTransformer<FetchCurrentFacility, BloodSugarSummaryViewEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(io)
          .map {
            val user = userSession.loggedInUserImmediate()!!
            facilityRepository.currentFacilityImmediate(user)
          }
          .map(::CurrentFacilityFetched)
    }
  }

  private fun fetchBloodSugarMeasurements(
      bloodSugarRepository: BloodSugarRepository,
      scheduler: Scheduler
  ): ObservableTransformer<FetchBloodSugarSummary, BloodSugarSummaryViewEvent> {
    return ObservableTransformer { effect ->
      effect
          .flatMap {
            bloodSugarRepository
                .latestMeasurements(it.patientUuid, config.numberOfBloodSugarsToDisplay)
                .subscribeOn(scheduler)
          }
          .map { BloodSugarSummaryFetched(it) }
    }
  }

  private fun fetchBloodSugarMeasurementsCount(
      scheduler: Scheduler
  ): ObservableTransformer<FetchBloodSugarCount, BloodSugarSummaryViewEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            bloodSugarRepository
                .bloodSugarsCount(it.patientUuid)
                .subscribeOn(scheduler)
          }
          .map(::BloodSugarCountFetched)
    }
  }
}
