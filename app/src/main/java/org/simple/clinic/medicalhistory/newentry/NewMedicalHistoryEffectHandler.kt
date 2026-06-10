package org.simple.clinic.medicalhistory.newentry

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Completable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.medicalhistory.OngoingMedicalHistoryEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patientattribute.BMIReading
import org.simple.clinic.patientattribute.PatientAttributeRepository
import org.simple.clinic.sync.DataSync
import org.simple.clinic.user.User
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import java.time.format.DateTimeFormatter
import javax.inject.Named

class NewMedicalHistoryEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val patientAttributeRepository: PatientAttributeRepository,
    private val dataSync: DataSync,
    private val currentUser: Lazy<User>,
    private val currentFacility: Lazy<Facility>,
    private val uuidGenerator: UuidGenerator,
    @Named("date_for_user_input") private val dateOfBirthFormatter: DateTimeFormatter,
    @Assisted private val viewEffectsConsumer: Consumer<NewMedicalHistoryViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<NewMedicalHistoryViewEffect>
    ): NewMedicalHistoryEffectHandler
  }

  fun build(): ObservableTransformer<NewMedicalHistoryEffect, NewMedicalHistoryEvent> {
    return RxMobius
        .subtypeEffectHandler<NewMedicalHistoryEffect, NewMedicalHistoryEvent>()
        .addTransformer(RegisterPatient::class.java, registerPatient(schedulersProvider.io()))
        .addTransformer(LoadOngoingPatientEntry::class.java, loadOngoingNewPatientEntry())
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility(schedulersProvider.io()))
        .addTransformer(TriggerSync::class.java, triggerSync())
        .addConsumer(NewMedicalHistoryViewEffect::class.java, viewEffectsConsumer::accept)
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
            val bmiReading = registerPatientEffect.bmiReading

            RegisterPatientData(loggedInUser, facility, ongoingMedicalHistoryEntry, bmiReading)
          }
          .map { registerPatientData ->
            patientRepository
                .saveOngoingEntryAsPatient(
                    patientEntry = patientRepository.ongoingEntry(),
                    loggedInUser = registerPatientData.user,
                    facility = registerPatientData.facility,
                    patientUuid = uuidGenerator.v4(),
                    addressUuid = uuidGenerator.v4(),
                    supplyUuidForBpPassport = uuidGenerator::v4,
                    supplyUuidForAlternativeId = uuidGenerator::v4,
                    supplyUuidForPhoneNumber = uuidGenerator::v4,
                    dateOfBirthFormatter = dateOfBirthFormatter
                ) to registerPatientData
          }
          .flatMapSingle { (registeredPatient, registeredPatientData) ->

            val saveBmi = Completable.fromAction {
              registeredPatientData.bmiReading?.let {
                patientAttributeRepository.save(
                    bmiReading = it,
                    patientUuid = registeredPatient.patientUuid,
                    loggedInUserUuid = registeredPatientData.user.uuid,
                    uuid = uuidGenerator.v4(),
                )
              }
            }

            medicalHistoryRepository
                .save(
                    uuid = uuidGenerator.v4(),
                    patientUuid = registeredPatient.patientUuid,
                    historyEntry = registeredPatientData.ongoingMedicalHistoryEntry
                )
                .andThen(saveBmi)
                .toSingleDefault(PatientRegistered(registeredPatient.patientUuid))
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
          .doOnNext { dataSync.fireAndForgetSync() }
          .map { SyncTriggered(it.registeredPatientUuid) }
    }
  }

  private data class RegisterPatientData(
      val user: User,
      val facility: Facility,
      val ongoingMedicalHistoryEntry: OngoingMedicalHistoryEntry,
      val bmiReading: BMIReading?
  )
}
