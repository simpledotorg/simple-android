package org.simple.clinic.bloodsugar.history

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider

class BloodSugarHistoryScreenEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val bloodSugarRepository: BloodSugarRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: BloodSugarHistoryScreenUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: BloodSugarHistoryScreenUiActions): BloodSugarHistoryScreenEffectHandler
  }

  fun build(): ObservableTransformer<BloodSugarHistoryScreenEffect, BloodSugarHistoryScreenEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodSugarHistoryScreenEffect, BloodSugarHistoryScreenEvent>()
        .addTransformer(LoadPatient::class.java, loadPatient(schedulersProvider.io()))
        .addTransformer(LoadBloodSugarHistory::class.java, loadBloodSugarHistory(schedulersProvider.io()))
        .addConsumer(OpenBloodSugarEntrySheet::class.java, { uiActions.openBloodSugarEntrySheet(it.patientUuid) }, schedulersProvider.ui())
        .build()
  }

  private fun loadBloodSugarHistory(
      scheduler: Scheduler
  ): ObservableTransformer<LoadBloodSugarHistory, BloodSugarHistoryScreenEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            bloodSugarRepository
                .allBloodSugars(it.patientUuid)
                .subscribeOn(scheduler)
          }
          .map(::BloodSugarHistoryLoaded)
    }
  }

  private fun loadPatient(
      scheduler: Scheduler
  ): ObservableTransformer<LoadPatient, BloodSugarHistoryScreenEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            patientRepository
                .patient(it.patientUuid)
                .take(1)
                .subscribeOn(scheduler)
          }
          .filterAndUnwrapJust()
          .map(::PatientLoaded)
    }
  }
}
