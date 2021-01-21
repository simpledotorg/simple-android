package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordInfo
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.user.User
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import java.time.Instant

class TeleconsultRecordEffectHandler @AssistedInject constructor(
    private val user: Lazy<User>,
    private val currentFacility: Lazy<Facility>,
    private val teleconsultRecordRepository: TeleconsultRecordRepository,
    private val patientRepository: PatientRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val schedulersProvider: SchedulersProvider,
    private val utcClock: UtcClock,
    private val uuidGenerator: UuidGenerator,
    @Assisted private val uiActions: UiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: UiActions): TeleconsultRecordEffectHandler
  }

  fun build(): ObservableTransformer<TeleconsultRecordEffect, TeleconsultRecordEvent> {
    return RxMobius.subtypeEffectHandler<TeleconsultRecordEffect, TeleconsultRecordEvent>()
        .addAction(GoBack::class.java, uiActions::goBackToPreviousScreen, schedulersProvider.ui())
        .addAction(NavigateToTeleconsultSuccess::class.java, { uiActions.navigateToTeleconsultSuccessScreen() }, schedulersProvider.ui())
        .addTransformer(LoadTeleconsultRecord::class.java, loadTeleconsultRecordDetails())
        .addTransformer(CreateTeleconsultRecord::class.java, createTeleconsultRecord())
        .addTransformer(LoadPatientDetails::class.java, loadPatientDetails())
        .addAction(ShowTeleconsultNotRecordedWarning::class.java, uiActions::showTeleconsultNotRecordedWarning, schedulersProvider.ui())
        .addTransformer(ValidateTeleconsultRecord::class.java, validateTeleconsultRecord())
        .addTransformer(ClonePatientPrescriptions::class.java, clonePatientPrescriptions())
        .build()
  }

  private fun clonePatientPrescriptions(): ObservableTransformer<ClonePatientPrescriptions, TeleconsultRecordEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { effect -> prescriptionRepository.newestPrescriptionsForPatientImmediate(effect.patientUuid) to effect }
          .doOnNext { (prescriptions, effect) -> clonePrescriptions(prescriptions, effect) }
          .map { PatientPrescriptionsCloned }
    }
  }

  private fun clonePrescriptions(
      prescriptions: List<PrescribedDrug>,
      effect: ClonePatientPrescriptions
  ) {
    if (prescriptions.isNotEmpty()) {
      prescriptionRepository.softDeletePrescriptions(prescriptions)

      val clonedPrescriptions = prescriptions.map { prescribedDrug ->
        prescribedDrug.refillForTeleconsultation(
            uuid = uuidGenerator.v4(),
            facilityUuid = currentFacility.get().uuid,
            teleconsultationId = effect.teleconsultRecordId,
            utcClock = utcClock
        )
      }

      prescriptionRepository.saveImmediate(clonedPrescriptions)
    }
  }

  private fun validateTeleconsultRecord(): ObservableTransformer<ValidateTeleconsultRecord, TeleconsultRecordEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map {
            val teleconsultRecord = teleconsultRecordRepository.getTeleconsultRecord(it.teleconsultRecordId)
            TeleconsultRecordValidated(teleconsultRecord != null)
          }
    }
  }

  private fun loadPatientDetails(): ObservableTransformer<LoadPatientDetails, TeleconsultRecordEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { patientRepository.patientImmediate(it.patientUuid) }
          .map(::PatientDetailsLoaded)
    }
  }

  private fun createTeleconsultRecord(): ObservableTransformer<CreateTeleconsultRecord, TeleconsultRecordEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { effect ->
            teleconsultRecordRepository.createTeleconsultRecordForMedicalOfficer(
                teleconsultRecordId = effect.teleconsultRecordId,
                patientUuid = effect.patientUuid,
                medicalOfficerId = user.get().uuid,
                teleconsultRecordInfo = createTeleconsultRecordInfo(effect)
            )

            TeleconsultRecordCreated(effect.teleconsultRecordId)
          }
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

  private fun loadTeleconsultRecordDetails(): ObservableTransformer<LoadTeleconsultRecord, TeleconsultRecordEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map {
            val teleconsultRecord = teleconsultRecordRepository.getTeleconsultRecord(it.teleconsultRecordId)
            TeleconsultRecordLoaded(teleconsultRecord)
          }
    }
  }
}
