package org.simple.clinic.bp.history

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider

class BloodPressureHistoryScreenEffectHandler @AssistedInject constructor(
    private val bloodPressureRepository: BloodPressureRepository,
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: BloodPressureHistoryScreenUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: BloodPressureHistoryScreenUiActions): BloodPressureHistoryScreenEffectHandler
  }

  fun build(): ObservableTransformer<BloodPressureHistoryScreenEffect, BloodPressureHistoryScreenEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodPressureHistoryScreenEffect, BloodPressureHistoryScreenEvent>()
        .addTransformer(LoadPatient::class.java, loadPatient(schedulersProvider.io()))
        .addTransformer(LoadBloodPressureHistory::class.java, loadBloodPressureHistory(schedulersProvider.io()))
        .addConsumer(OpenBloodPressureEntrySheet::class.java, { uiActions.openBloodPressureEntrySheet(it.patientUuid) }, schedulersProvider.ui())
        .addConsumer(OpenBloodPressureUpdateSheet::class.java, { uiActions.openBloodPressureUpdateSheet(it.bloodPressureMeasurement.uuid) }, schedulersProvider.ui())
        .build()
  }

  private fun loadBloodPressureHistory(
      scheduler: Scheduler
  ): ObservableTransformer<LoadBloodPressureHistory, BloodPressureHistoryScreenEvent> {
    return ObservableTransformer { effect ->
      effect
          .switchMap {
            bloodPressureRepository
                .allBloodPressures(it.patientUuid)
                .subscribeOn(scheduler)
          }
          .map(::BloodPressureHistoryLoaded)
    }
  }

  private fun loadPatient(
      scheduler: Scheduler
  ): ObservableTransformer<LoadPatient, BloodPressureHistoryScreenEvent> {
    return ObservableTransformer { effect ->
      effect
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
