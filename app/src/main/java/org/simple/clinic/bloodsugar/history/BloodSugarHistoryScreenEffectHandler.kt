package org.simple.clinic.bloodsugar.history

import androidx.paging.PositionalDataSource
import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.bloodsugar.BloodSugarHistoryListItemDataSourceFactory
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider

class BloodSugarHistoryScreenEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val bloodSugarRepository: BloodSugarRepository,
    private val schedulersProvider: SchedulersProvider,
    private val dataSourceFactory: BloodSugarHistoryListItemDataSourceFactory.Factory,
    @Assisted private val viewEffectsConsumer: Consumer<BloodSugarHistoryScreenViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<BloodSugarHistoryScreenViewEffect>
    ): BloodSugarHistoryScreenEffectHandler
  }

  fun build(): ObservableTransformer<BloodSugarHistoryScreenEffect, BloodSugarHistoryScreenEvent> {
    return RxMobius
        .subtypeEffectHandler<BloodSugarHistoryScreenEffect, BloodSugarHistoryScreenEvent>()
        .addTransformer(LoadPatient::class.java, loadPatient(schedulersProvider.io()))
        .addConsumer(BloodSugarHistoryScreenViewEffect::class.java, viewEffectsConsumer::accept)
        .addTransformer(LoadBloodSugarHistory::class.java, loadBloodSugarHistory())
        .build()
  }

  private fun loadBloodSugarHistory(): ObservableTransformer<LoadBloodSugarHistory, BloodSugarHistoryScreenEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map {
            val dataSource = bloodSugarRepository
                .allBloodSugarsDataSource(it.patientUuid)
                .create() as PositionalDataSource<BloodSugarMeasurement>

            dataSourceFactory.create(dataSource)
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
