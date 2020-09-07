package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordInfo
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.user.User
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.Instant

class TeleconsultRecordEffectHandler @AssistedInject constructor(
    private val user: Lazy<User>,
    private val teleconsultRecordRepository: TeleconsultRecordRepository,
    private val schedulersProvider: SchedulersProvider,
    private val utcClock: UtcClock,
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): TeleconsultRecordEffectHandler
  }

  fun build(): ObservableTransformer<TeleconsultRecordEffect, TeleconsultRecordEvent> {
    return RxMobius.subtypeEffectHandler<TeleconsultRecordEffect, TeleconsultRecordEvent>()
        .addAction(GoBack::class.java, uiActions::goBackToPreviousScreen)
        .addAction(NavigateToTeleconsultSuccess::class.java, { uiActions.navigateToTeleconsultSuccessScreen() }, schedulersProvider.ui())
        .addTransformer(LoadTeleconsultRecordWithPrescribedDrugs::class.java, loadTeleconsultRecordWithPrescribedDrugs())
        .addTransformer(CreateTeleconsultRecord::class.java, createTeleconsultRecord())
        .build()
  }

  private fun createTeleconsultRecord(): ObservableTransformer<CreateTeleconsultRecord, TeleconsultRecordEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { effect ->
            teleconsultRecordRepository.createTeleconsultRecordForMedicalOfficer(
                teleconsultRecordId = effect.teleconsultRecordId,
                patientUuid = effect.patientUuid,
                medicalOfficerId = user.get().uuid,
                teleconsultRecordInfo = createTeleconsultRecordInfo(effect)
            )
          }
          .map { TeleconsultRecordCreated }
    }
  }

  private fun createTeleconsultRecordInfo(it: CreateTeleconsultRecord): TeleconsultRecordInfo {
    return TeleconsultRecordInfo(
        recordedAt = Instant.now(utcClock),
        teleconsultationType = it.teleconsultationType,
        patientTookMedicines = it.patientTookMedicine,
        patientConsented = it.patientConsented,
        medicalOfficerNumber = null
    )
  }

  private fun loadTeleconsultRecordWithPrescribedDrugs(): ObservableTransformer<LoadTeleconsultRecordWithPrescribedDrugs, TeleconsultRecordEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { teleconsultRecordRepository.getTeleconsultRecordWithPrescribedDrugs(it.teleconsultRecordId) }
          .map(::TeleconsultRecordWithPrescribedDrugsLoaded)
    }
  }
}
