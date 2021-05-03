package org.simple.clinic.shortcodesearchresult

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.searchresultsview.PartitionSearchResultsByVisitedFacility
import org.simple.clinic.util.scheduler.SchedulersProvider

class IdentifierSearchResultEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val currentFacility: Lazy<Facility>,
    private val bloodPressureDao: BloodPressureMeasurement.RoomDao,
    @Assisted private val uiActions: UiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: UiActions): IdentifierSearchResultEffectHandler
  }

  fun build(): ObservableTransformer<IdentifierSearchResultEffect, IdentifierSearchResultEvent> {
    return RxMobius
        .subtypeEffectHandler<IdentifierSearchResultEffect, IdentifierSearchResultEvent>()
        .addConsumer(OpenPatientSummary::class.java, { uiActions.openPatientSummary(it.patientId) }, schedulers.ui())
        .addAction(OpenPatientSearch::class.java, uiActions::openPatientSearch, schedulers.ui())
        .addTransformer(SearchByShortCode::class.java, searchByShortCode())
        .build()
  }

  private fun searchByShortCode(): ObservableTransformer<SearchByShortCode, IdentifierSearchResultEvent> {
    return ObservableTransformer { effects ->
      val currentFacilityStream = Observable
          .fromCallable { currentFacility.get() }
          .subscribeOn(schedulers.io())

      effects
          .map { effect -> effect.shortCode }
          .switchMap { shortCode ->
            patientRepository
                .searchByShortCode(shortCode)
                .subscribeOn(schedulers.io())
          }
          .compose(PartitionSearchResultsByVisitedFacility(bloodPressureDao, currentFacilityStream))
          .map(::ShortCodeSearchCompleted)
    }
  }
}
