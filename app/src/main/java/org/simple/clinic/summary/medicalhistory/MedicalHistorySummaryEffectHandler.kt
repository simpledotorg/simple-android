package org.simple.clinic.summary.medicalhistory

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.threeten.bp.Instant

class MedicalHistorySummaryEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val clock: UtcClock,
    @Assisted private val uiActions: MedicalHistorySummaryUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: MedicalHistorySummaryUiActions): MedicalHistorySummaryEffectHandler
  }

  fun build(): ObservableTransformer<MedicalHistorySummaryEffect, MedicalHistorySummaryEvent> {
    return RxMobius
        .subtypeEffectHandler<MedicalHistorySummaryEffect, MedicalHistorySummaryEvent>()
        .addTransformer(LoadMedicalHistory::class.java, loadMedicalHistory())
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility())
        .addAction(HideDiagnosisError::class.java, uiActions::hideDiagnosisError, schedulers.ui())
        .addTransformer(SaveUpdatedMedicalHistory::class.java, updateMedicalHistory())
        .build()
  }

  private fun loadMedicalHistory(): ObservableTransformer<LoadMedicalHistory, MedicalHistorySummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { medicalHistoryRepository.historyForPatientOrDefault(it.patientUUID).subscribeOn(schedulers.io()) }
          .map(::MedicalHistoryLoaded)
    }
  }

  private fun loadCurrentFacility(): ObservableTransformer<LoadCurrentFacility, MedicalHistorySummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { facilityRepository.currentFacilityImmediate(userSession.loggedInUserImmediate()!!) }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun updateMedicalHistory(): ObservableTransformer<SaveUpdatedMedicalHistory, MedicalHistorySummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .flatMapCompletable { medicalHistoryRepository.save(it.medicalHistory, Instant.now(clock)).subscribeOn(schedulers.io()) }
          .andThen(Observable.empty())
    }
  }
}
