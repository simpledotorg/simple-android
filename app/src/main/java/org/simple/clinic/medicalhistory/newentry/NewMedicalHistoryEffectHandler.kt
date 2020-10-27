package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.SyncGroup.FREQUENT
import org.simple.clinic.user.User
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator

class NewMedicalHistoryEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: NewMedicalHistoryUiActions,
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val dataSync: DataSync,
    private val currentUser: Lazy<User>,
    private val currentFacility: Lazy<Facility>,
    private val uuidGenerator: UuidGenerator
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: NewMedicalHistoryUiActions): NewMedicalHistoryEffectHandler
  }

  fun build(): ObservableTransformer<NewMedicalHistoryEffect, NewMedicalHistoryEvent> {
    return RxMobius
        .subtypeEffectHandler<NewMedicalHistoryEffect, NewMedicalHistoryEvent>()
        .addConsumer(OpenPatientSummaryScreen::class.java, { effect -> uiActions.openPatientSummaryScreen(effect.patientUuid) }, schedulersProvider.ui())
        .addTransformer(RegisterPatient::class.java, registerPatient(schedulersProvider.io()))
        .addTransformer(LoadOngoingPatientEntry::class.java, loadOngoingNewPatientEntry())
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility(schedulersProvider.io()))
        .addTransformer(TriggerSync::class.java, triggerSync())
        .build()
  }

  private fun registerPatient(scheduler: Scheduler): ObservableTransformer<RegisterPatient, NewMedicalHistoryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { registerPatientEffect ->
            val loggedInUser = currentUser.get()
            val facility = currentFacility.get()
            val ongoingMedicalHistoryEntry = registerPatientEffect.ongoingMedicalHistoryEntry

            RegisterPatientData(loggedInUser, facility, ongoingMedicalHistoryEntry)
          }
          .map { (user, facility, ongoingMedicalHistoryEntry) ->
            patientRepository
                .saveOngoingEntryAsPatient(
                    loggedInUser = user,
                    facility = facility,
                    patientUuid = uuidGenerator.v4(),
                    addressUuid = uuidGenerator.v4(),
                    supplyUuidForBpPassport = uuidGenerator::v4,
                    supplyUuidForAlternativeId = uuidGenerator::v4,
                    supplyUuidForPhoneNumber = uuidGenerator::v4
                ) to ongoingMedicalHistoryEntry
          }
          .flatMapSingle { (registeredPatient, ongoingMedicalHistoryEntry) ->
            medicalHistoryRepository
                .save(
                    uuid = uuidGenerator.v4(),
                    patientUuid = registeredPatient.uuid,
                    historyEntry = ongoingMedicalHistoryEntry
                )
                .toSingleDefault(PatientRegistered(registeredPatient.uuid))
          }
    }
  }

  private fun loadOngoingNewPatientEntry(): ObservableTransformer<LoadOngoingPatientEntry, NewMedicalHistoryEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { patientRepository.ongoingEntry() }
          .map(::OngoingPatientEntryLoaded)
    }
  }

  private fun loadCurrentFacility(scheduler: Scheduler): ObservableTransformer<LoadCurrentFacility, NewMedicalHistoryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { currentFacility.get() }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun triggerSync(): ObservableTransformer<TriggerSync, NewMedicalHistoryEvent> {
    return ObservableTransformer { effects ->
      effects
          .doOnNext { dataSync.fireAndForgetSync(FREQUENT) }
          .map { SyncTriggered(it.registeredPatientUuid) }
    }
  }

  private data class RegisterPatientData(
      val user: User,
      val facility: Facility,
      val ongoingMedicalHistoryEntry: OngoingMedicalHistoryEntry
  )
}
