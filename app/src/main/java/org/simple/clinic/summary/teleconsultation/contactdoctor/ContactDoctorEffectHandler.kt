package org.simple.clinic.summary.teleconsultation.contactdoctor

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.summary.PatientTeleconsultationInfo
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityRepository
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRequestInfo
import org.simple.clinic.user.User
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toNullable
import org.simple.clinic.uuid.UuidGenerator
import java.time.Instant

class ContactDoctorEffectHandler @AssistedInject constructor(
    private val currentUser: Lazy<User>,
    private val currentFacility: Lazy<Facility>,
    private val teleconsultationFacilityRepository: TeleconsultationFacilityRepository,
    private val teleconsultRecordRepository: TeleconsultRecordRepository,
    private val patientRepository: PatientRepository,
    private val bloodPressureRepository: BloodPressureRepository,
    private val bloodSugarRepository: BloodSugarRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val patientSummaryConfig: PatientSummaryConfig,
    private val uuidGenerator: UuidGenerator,
    private val clock: UtcClock,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: ContactDoctorUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: ContactDoctorUiActions): ContactDoctorEffectHandler
  }

  fun build(): ObservableTransformer<ContactDoctorEffect, ContactDoctorEvent> = RxMobius
      .subtypeEffectHandler<ContactDoctorEffect, ContactDoctorEvent>()
      .addTransformer(LoadMedicalOfficers::class.java, loadMedicalOfficers())
      .addTransformer(CreateTeleconsultRequest::class.java, createTeleconsultRequest())
      .addTransformer(LoadPatientTeleconsultInfo::class.java, loadPatientTeleconsultInfo())
      .addConsumer(SendTeleconsultMessage::class.java, {
        uiActions.sendTeleconsultMessage(it.teleconsultInfo, it.messageTarget)
      }, schedulersProvider.ui())
      .build()

  private fun loadPatientTeleconsultInfo(): ObservableTransformer<LoadPatientTeleconsultInfo, ContactDoctorEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map(::createPatientTeleconsultInfo)
          .map { (patientTeleconsultInfo, messageTarget) ->
            PatientTeleconsultInfoLoaded(patientTeleconsultInfo, messageTarget)
          }
    }
  }

  private fun createPatientTeleconsultInfo(effect: LoadPatientTeleconsultInfo): Pair<PatientTeleconsultationInfo, MessageTarget> {
    val patientUuid = effect.patientUuid
    val bpPassportForPatient = patientRepository.bpPassportForPatient(effect.patientUuid)
    val bloodPressures = bloodPressureRepository.newestMeasurementsForPatientImmediate(patientUuid, patientSummaryConfig.numberOfMeasurementsForTeleconsultation)
    val prescriptions = prescriptionRepository.newestPrescriptionsForPatientImmediate(patientUuid)
    val bloodSugars = bloodSugarRepository.latestMeasurementsImmediate(patientUuid, patientSummaryConfig.numberOfMeasurementsForTeleconsultation)
    val medicalHistory = medicalHistoryRepository.historyForPatientOrDefaultImmediate(
        defaultHistoryUuid = uuidGenerator.v4(),
        patientUuid = patientUuid
    )
    val facility = currentFacility.get()
    val userPhoneNumber = currentUser.get().phoneNumber

    return PatientTeleconsultationInfo(
        patientUuid = patientUuid,
        teleconsultRecordId = effect.teleconsultRecordId,
        bpPassport = bpPassportForPatient.toNullable()?.identifier?.displayValue(),
        facility = facility,
        bloodPressures = bloodPressures,
        bloodSugars = bloodSugars,
        prescriptions = prescriptions,
        medicalHistory = medicalHistory,
        nursePhoneNumber = userPhoneNumber,
        doctorPhoneNumber = effect.doctorPhoneNumber
    ) to effect.messageTarget
  }

  private fun createTeleconsultRequest(): ObservableTransformer<CreateTeleconsultRequest, ContactDoctorEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map {
            val teleconsultRecordId = uuidGenerator.v4()
            val teleconsultRequestInfo = TeleconsultRequestInfo(
                requesterId = currentUser.get().uuid,
                facilityId = currentFacility.get().uuid,
                requestedAt = Instant.now(clock),
                requesterCompletionStatus = null
            )

            teleconsultRecordRepository.createTeleconsultRequestForNurse(
                teleconsultRecordId = teleconsultRecordId,
                patientUuid = it.patientUuid,
                medicalOfficerId = it.medicalOfficerId,
                teleconsultRequestInfo = teleconsultRequestInfo
            )

            teleconsultRecordId to it
          }
          .map { (teleconsultRecordId, effect) ->
            TeleconsultRequestCreated(
                teleconsultRecordId = teleconsultRecordId,
                doctorPhoneNumber = effect.doctorPhoneNumber,
                messageTarget = effect.messageTarget
            )
          }
    }
  }

  private fun loadMedicalOfficers(): ObservableTransformer<LoadMedicalOfficers, ContactDoctorEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { teleconsultationFacilityRepository.medicalOfficersForFacility(currentFacility.get().uuid) }
          .map(::MedicalOfficersLoaded)
    }
  }
}
