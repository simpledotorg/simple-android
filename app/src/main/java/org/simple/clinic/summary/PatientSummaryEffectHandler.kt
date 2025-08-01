package org.simple.clinic.summary

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.appconfig.Country
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.cvdrisk.CVDRiskRange
import org.simple.clinic.cvdrisk.CVDRiskRepository
import org.simple.clinic.cvdrisk.LabBasedCVDRiskInput
import org.simple.clinic.cvdrisk.NonLabBasedCVDRiskInput
import org.simple.clinic.cvdrisk.calculator.LabBasedCVDRiskCalculator
import org.simple.clinic.cvdrisk.calculator.NonLabBasedCVDRiskCalculator
import org.simple.clinic.drugs.DiagnosisWarningPrescriptions
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.Answer
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patientattribute.PatientAttributeRepository
import org.simple.clinic.summary.addphone.MissingPhoneReminderRepository
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityRepository
import org.simple.clinic.sync.DataSync
import org.simple.clinic.user.User
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toNullable
import org.simple.clinic.uuid.UuidGenerator
import java.time.Instant
import java.time.LocalDate
import java.util.Optional
import java.util.UUID
import java.util.function.Function
import javax.inject.Provider
import org.simple.clinic.medicalhistory.Answer as MedicalHistoryAnswer

class PatientSummaryEffectHandler @AssistedInject constructor(
    private val clock: UtcClock,
    private val userClock: UserClock,
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val bloodPressureRepository: BloodPressureRepository,
    private val appointmentRepository: AppointmentRepository,
    private val missingPhoneReminderRepository: MissingPhoneReminderRepository,
    private val bloodSugarRepository: BloodSugarRepository,
    private val dataSync: DataSync,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val cvdRiskRepository: CVDRiskRepository,
    private val patientAttributeRepository: PatientAttributeRepository,
    private val country: Country,
    private val currentUser: Lazy<User>,
    private val currentFacility: Lazy<Facility>,
    private val uuidGenerator: UuidGenerator,
    private val facilityRepository: FacilityRepository,
    private val teleconsultationFacilityRepository: TeleconsultationFacilityRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val cdssPilotFacilities: Lazy<List<UUID>>,
    private val diagnosisWarningPrescriptions: Provider<DiagnosisWarningPrescriptions>,
    private val nonLabBasedCVDRiskCalculator: NonLabBasedCVDRiskCalculator,
    private val labBasedCVDRiskCalculator: LabBasedCVDRiskCalculator,
    @Assisted private val viewEffectsConsumer: Consumer<PatientSummaryViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<PatientSummaryViewEffect>
    ): PatientSummaryEffectHandler
  }

  fun build(): ObservableTransformer<PatientSummaryEffect, PatientSummaryEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientSummaryEffect, PatientSummaryEvent>()
        .addTransformer(LoadPatientSummaryProfile::class.java, loadPatientSummaryProfile(schedulersProvider.io()))
        .addTransformer(LoadCurrentUserAndFacility::class.java, loadUserAndCurrentFacility())
        .addTransformer(CheckForInvalidPhone::class.java, checkForInvalidPhone(schedulersProvider.io()))
        .addTransformer(MarkReminderAsShown::class.java, markReminderAsShown(schedulersProvider.io()))
        .addTransformer(LoadDataForBackClick::class.java, loadDataForBackClick(schedulersProvider.io()))
        .addTransformer(LoadDataForDoneClick::class.java, loadDataForDoneClick(schedulersProvider.io()))
        .addTransformer(TriggerSync::class.java, triggerSync())
        .addTransformer(FetchHasShownMissingPhoneReminder::class.java, fetchHasShownMissingPhoneReminder(schedulersProvider.io()))
        .addTransformer(LoadMedicalOfficers::class.java, loadMedicalOfficers())
        .addTransformer(LoadClinicalDecisionSupportInfo::class.java, loadClinicalDecisionSupport())
        .addConsumer(PatientSummaryViewEffect::class.java, viewEffectsConsumer::accept)
        .addTransformer(LoadPatientRegistrationData::class.java, checkPatientRegistrationData())
        .addTransformer(CheckIfCDSSPilotIsEnabled::class.java, checkIfCDSSPilotIsEnabled())
        .addTransformer(LoadLatestScheduledAppointment::class.java, loadLatestScheduledAppointment())
        .addConsumer(UpdatePatientReassignmentStatus::class.java, { updatePatientReassignmentState(it.patientUuid, it.status) }, schedulersProvider.io())
        .addTransformer(CheckPatientReassignmentStatus::class.java, checkPatientReassignmentStatus())
        .addConsumer(MarkDiabetesDiagnosis::class.java, { markDiabetesDiagnosis(it.patientUuid) }, schedulersProvider.io())
        .addConsumer(MarkHypertensionDiagnosis::class.java, { markHypertension(it.patientUuid) }, schedulersProvider.io())
        .addTransformer(LoadStatinPrescriptionCheckInfo::class.java, loadStatinPrescriptionCheckInfo())
        .addTransformer(CalculateNonLabBasedCVDRisk::class.java, calculateNonLabBasedCVDRisk())
        .addTransformer(CalculateLabBasedCVDRisk::class.java, calculateLabBasedCVDRisk())
        .addTransformer(UpdateCVDRisk::class.java, updateCVDRisk())
        .addTransformer(SaveCVDRisk::class.java, saveCVDRisk())
        .addTransformer(LoadStatinInfo::class.java, loadStatinInfo())
        .addConsumer(UpdateSmokingStatus::class.java, { updateSmokingStatus(it.patientId, it.isSmoker) }, schedulersProvider.io())
        .build()
  }

  private fun loadStatinPrescriptionCheckInfo(): ObservableTransformer<LoadStatinPrescriptionCheckInfo, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap { effect ->
            val patient = effect.patient
            Observable.combineLatest(
                medicalHistoryRepository.historyForPatientOrDefault(
                    defaultHistoryUuid = uuidGenerator.v4(),
                    patientUuid = patient.uuid
                ),
                prescriptionRepository.newestPrescriptionsForPatient(patient.uuid),
                bloodPressureRepository.newestMeasurementsForPatient(
                    patientUuid = patient.uuid,
                    limit = 1
                ),
            ) { medicalHistory, prescriptions, newestBp ->
              val cvdRisk = cvdRiskRepository.getCVDRiskImmediate(patient.uuid)

              val patientAttribute = patientAttributeRepository.getPatientAttributeImmediate(
                  patientUuid = patient.uuid
              )

              val ninetyDaysAgo = LocalDate.now(userClock)
                  .minusDays(90)
                  .atStartOfDay(userClock.zone)
                  .toInstant()

              val hasMedicalHistoryChanged = cvdRisk?.let { risk ->
                medicalHistory.updatedAt > risk.timestamps.updatedAt
              } ?: false

              val wasBPMeasuredWithin90Days = newestBp.firstOrNull()?.updatedAt?.let { updatedAt ->
                updatedAt > ninetyDaysAgo
              } ?: false

              val wasCVDCalculatedWithin90Days = cvdRisk?.let { risk ->
                risk.timestamps.updatedAt > ninetyDaysAgo
              } ?: false

              StatinPrescriptionCheckInfoLoaded(
                  age = patient.ageDetails.estimateAge(userClock),
                  isPatientDead = patient.status == PatientStatus.Dead,
                  cvdRiskRange = cvdRisk?.riskScore,
                  medicalHistory = medicalHistory,
                  patientAttribute = patientAttribute,
                  prescriptions = prescriptions,
                  wasBPMeasuredWithin90Days = wasBPMeasuredWithin90Days,
                  hasMedicalHistoryChanged = hasMedicalHistoryChanged,
                  wasCVDCalculatedWithin90Days = wasCVDCalculatedWithin90Days,
              )
            }
          }
    }
  }

  private fun calculateNonLabBasedCVDRisk(): ObservableTransformer<CalculateNonLabBasedCVDRisk, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { effect ->
            val patient = effect.patient
            val bloodPressure = bloodPressureRepository
                .newestMeasurementsForPatientImmediate(patient.uuid, 1).firstOrNull()

            val patientAttribute = patientAttributeRepository.getPatientAttributeImmediate(
                patientUuid = patient.uuid
            )
            val medicalHistory = medicalHistoryRepository.historyForPatientOrDefaultImmediate(
                defaultHistoryUuid = uuidGenerator.v4(),
                patientUuid = patient.uuid
            )

            var risk: CVDRiskRange? = null

            if (bloodPressure != null) {
              risk = nonLabBasedCVDRiskCalculator.calculateCvdRisk(
                  NonLabBasedCVDRiskInput(
                      gender = patient.gender,
                      age = patient.ageDetails.estimateAge(userClock),
                      systolic = bloodPressure.reading.systolic,
                      isSmoker = medicalHistory.isSmoking,
                      bmi = patientAttribute?.bmiReading?.calculateBMI(),
                  )
              )
            }

            val existingCvdRisk = cvdRiskRepository.getCVDRiskImmediate(patient.uuid)
            CVDRiskCalculated(existingCvdRisk, risk)
          }
    }
  }

  private fun calculateLabBasedCVDRisk(): ObservableTransformer<CalculateLabBasedCVDRisk, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { effect ->
            val patient = effect.patient
            val bloodPressure = bloodPressureRepository
                .newestMeasurementsForPatientImmediate(patient.uuid, 1).firstOrNull()

            val medicalHistory = medicalHistoryRepository.historyForPatientOrDefaultImmediate(
                defaultHistoryUuid = uuidGenerator.v4(),
                patientUuid = patient.uuid
            )

            var risk: CVDRiskRange? = null

            if (bloodPressure != null) {
              risk = labBasedCVDRiskCalculator.calculateCvdRisk(
                  LabBasedCVDRiskInput(
                      gender = patient.gender,
                      age = patient.ageDetails.estimateAge(userClock),
                      systolic = bloodPressure.reading.systolic,
                      isSmoker = medicalHistory.isSmoking,
                      diagnosedWithDiabetes = medicalHistory.diagnosedWithDiabetes,
                      cholesterol = medicalHistory.cholesterol?.let { MedicalHistory.convertCholesterolToMmol(it) }
                  )
              )
            }

            val existingCvdRisk = cvdRiskRepository.getCVDRiskImmediate(patient.uuid)
            CVDRiskCalculated(existingCvdRisk, risk)
          }
    }
  }

  private fun updateCVDRisk(): ObservableTransformer<UpdateCVDRisk, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map {
            cvdRiskRepository.save(
                it.oldRisk.copy(riskScore = it.newRiskRange),
                updateAt = Instant.now(clock)
            )
            CVDRiskUpdated
          }
    }
  }

  private fun saveCVDRisk(): ObservableTransformer<SaveCVDRisk, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map {
            cvdRiskRepository.save(
                riskScore = it.cvdRiskRange,
                patientUuid = it.patientUuid,
                uuid = uuidGenerator.v4()
            )
            CVDRiskUpdated
          }
    }
  }

  private fun loadStatinInfo(): ObservableTransformer<LoadStatinInfo, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { effect ->
            val patientUuid = effect.patientUuid
            val patient = patientRepository.patientImmediate(patientUuid)
            val medicalHistory = medicalHistoryRepository.historyForPatientOrDefaultImmediate(
                defaultHistoryUuid = uuidGenerator.v4(),
                patientUuid = patientUuid
            )
            val patientAttribute = patientAttributeRepository.getPatientAttributeImmediate(patientUuid)
            val riskRange = cvdRiskRepository.getCVDRiskImmediate(patientUuid)?.riskScore
            val canPrescribeStatin = if (country.isoCountryCode == Country.SRI_LANKA) {
              riskRange?.canPrescribeStatinInSriLanka ?: false
            } else {
              riskRange?.canPrescribeStatin ?: false || medicalHistory.diagnosedWithDiabetes == MedicalHistoryAnswer.Yes
            }

            StatinInfoLoaded(
                age = patient!!.ageDetails.estimateAge(userClock),
                medicalHistory = medicalHistory,
                canPrescribeStatin = canPrescribeStatin,
                riskRange = riskRange,
                bmiReading = patientAttribute?.bmiReading,
            )
          }
    }
  }

  private fun updateSmokingStatus(patientUuid: UUID, isSmoker: MedicalHistoryAnswer) {
    val medicalHistory = medicalHistoryRepository.historyForPatientOrDefaultImmediate(
        patientUuid = patientUuid,
        defaultHistoryUuid = uuidGenerator.v4()
    )
    val updatedMedicalHistory = medicalHistory.answered(
        question = MedicalHistoryQuestion.IsSmoking,
        answer = isSmoker
    )

    medicalHistoryRepository.save(updatedMedicalHistory, Instant.now(clock))
  }

  private fun markHypertension(patientUuid: UUID) {
    val medicalHistory = medicalHistoryRepository.historyForPatientOrDefaultImmediate(
        patientUuid = patientUuid,
        defaultHistoryUuid = uuidGenerator.v4()
    )
    val updatedMedicalHistory = medicalHistory.answered(
        question = MedicalHistoryQuestion.DiagnosedWithHypertension,
        answer = MedicalHistoryAnswer.Yes
    )

    medicalHistoryRepository.save(updatedMedicalHistory, Instant.now(clock))
  }

  private fun markDiabetesDiagnosis(patientUuid: UUID) {
    val medicalHistory = medicalHistoryRepository.historyForPatientOrDefaultImmediate(
        patientUuid = patientUuid,
        defaultHistoryUuid = uuidGenerator.v4()
    )
    val updatedMedicalHistory = medicalHistory.answered(
        question = MedicalHistoryQuestion.DiagnosedWithDiabetes,
        answer = MedicalHistoryAnswer.Yes
    )

    medicalHistoryRepository.save(updatedMedicalHistory, Instant.now(clock))
  }

  private fun checkPatientReassignmentStatus(): ObservableTransformer<CheckPatientReassignmentStatus, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map {
            val isPatientEligibleForReassignment = patientRepository.isPatientEligibleForReassignment(it.patientUuid)
            PatientReassignmentStatusLoaded(
                isPatientEligibleForReassignment = isPatientEligibleForReassignment,
                clickAction = it.clickAction,
                screenCreatedTimestamp = it.screenCreatedTimestamp
            )
          }
    }
  }

  private fun updatePatientReassignmentState(patientUuid: UUID, status: Answer) {
    patientRepository.updatePatientReassignmentEligibilityStatus(patientUuid, status)
  }

  private fun loadLatestScheduledAppointment(): ObservableTransformer<LoadLatestScheduledAppointment, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map {
            val appointment = appointmentRepository.latestScheduledAppointmentForPatient(it.patientUuid)
            LatestScheduledAppointmentLoaded(appointment)
          }
    }
  }

  private fun checkIfCDSSPilotIsEnabled(): ObservableTransformer<CheckIfCDSSPilotIsEnabled, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map {
            val currentFacilityId = currentFacility.get().uuid
            CDSSPilotStatusChecked(isPilotEnabledForFacility =
            country.isoCountryCode == Country.ETHIOPIA ||
                country.isoCountryCode == Country.SRI_LANKA ||
                cdssPilotFacilities.get().contains(currentFacilityId)
            )
          }
    }
  }

  private fun loadClinicalDecisionSupport(): ObservableTransformer<LoadClinicalDecisionSupportInfo, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap {
            val patientUuid = it.patientUuid
            bloodPressureRepository.isNewestBpEntryHigh(patientUuid).map { Pair(patientUuid, it) }
          }
          .switchMap { (patientUuid, isNewestBpEntryHigh) ->
            prescriptionRepository.hasPrescriptionForPatientChangedToday(patientUuid).map { Pair(isNewestBpEntryHigh, it) }
          }
          .map { (isNewestBpEntryHigh, hasPrescriptionsChangedToday) ->
            ClinicalDecisionSupportInfoLoaded(isNewestBpEntryHigh, hasPrescriptionsChangedToday)
          }
    }
  }

  private fun checkPatientRegistrationData(): ObservableTransformer<LoadPatientRegistrationData, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { effect ->
            val patientUuid = effect.patientUuid
            val countOfPrescribedDrugs = prescriptionRepository.prescriptionCountImmediate(patientUuid)
            val countOfRecordedBloodPressures = bloodPressureRepository.bloodPressureCountImmediate(patientUuid)
            val countOfRecordedBloodSugars = bloodSugarRepository.bloodSugarCountImmediate(patientUuid)

            PatientRegistrationDataLoaded(
                countOfPrescribedDrugs = countOfPrescribedDrugs,
                countOfRecordedBloodPressures = countOfRecordedBloodPressures,
                countOfRecordedBloodSugars = countOfRecordedBloodSugars
            )
          }
    }
  }

  private fun loadMedicalOfficers(): ObservableTransformer<LoadMedicalOfficers, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { teleconsultationFacilityRepository.medicalOfficersForFacility(currentFacility.get().uuid) }
          .map(::MedicalOfficersLoaded)
    }
  }

  private fun loadUserAndCurrentFacility(): ObservableTransformer<LoadCurrentUserAndFacility, PatientSummaryEvent> {
    return ObservableTransformer { effectsStream ->
      effectsStream
          .observeOn(schedulersProvider.io())
          .map {
            val user = currentUser.get()
            val facility = currentFacility.get()

            CurrentUserAndFacilityLoaded(user, facility)
          }
    }
  }

  private fun loadPatientSummaryProfile(scheduler: Scheduler): ObservableTransformer<LoadPatientSummaryProfile, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .switchMap { patientRepository.patientProfile(it.patientUuid) }
          .extractIfPresent()
          .map { it.withoutDeletedBusinessIds().withoutDeletedPhoneNumbers() }
          .map { patientProfile ->
            val registeredFacility = getRegisteredFacility(patientProfile.patient.registeredFacilityId)
            patientProfile to registeredFacility
          }
          .map { (patientProfile, facility) ->
            mapPatientProfileToSummaryProfile(patientProfile, facility)
          }
          .map(::PatientSummaryProfileLoaded)
    }
  }

  private fun getRegisteredFacility(patientRegisteredFacilityId: UUID?): Optional<Facility> {
    return Optional
        .ofNullable(patientRegisteredFacilityId)
        .flatMap(Function { facilityRepository.facility(it) })
  }

  private fun getAssignedFacility(assignedFacilityId: UUID?): Optional<Facility> {
    return Optional
        .ofNullable(assignedFacilityId)
        .flatMap { facilityRepository.facility(it) }
  }

  private fun mapPatientProfileToSummaryProfile(
      patientProfile: PatientProfile,
      facility: Optional<Facility>
  ): PatientSummaryProfile {
    return PatientSummaryProfile(
        patient = patientProfile.patient,
        address = patientProfile.address,
        phoneNumber = patientProfile.phoneNumbers.firstOrNull(),
        bpPassport = patientProfile.businessIds.filter { it.identifier.type == BpPassport }.maxByOrNull { it.createdAt },
        alternativeId = patientProfile.businessIds.filter { it.identifier.type == country.alternativeIdentifierType }.maxByOrNull { it.createdAt },
        facility = facility.toNullable(),
    )
  }

  private fun checkForInvalidPhone(
      backgroundWorkScheduler: Scheduler
  ): ObservableTransformer<CheckForInvalidPhone, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(backgroundWorkScheduler)
          .map { hasInvalidPhone(it.patientUuid) }
          .map(::CompletedCheckForInvalidPhone)
    }
  }

  // TODO(vs): 2020-02-19 Revisit after Mobius migration
  private fun markReminderAsShown(
      scheduler: Scheduler
  ): ObservableTransformer<MarkReminderAsShown, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .flatMap { effect ->
            missingPhoneReminderRepository
                .markReminderAsShownFor(effect.patientUuid)
                .subscribeOn(scheduler)
                .andThen(Observable.empty<PatientSummaryEvent>())
          }
    }
  }

  private fun loadDataForBackClick(
      scheduler: Scheduler
  ): ObservableTransformer<LoadDataForBackClick, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { loadDataForBackClick ->
            val patientUuid = loadDataForBackClick.patientUuid
            val timestamp = loadDataForBackClick.screenCreatedTimestamp
            val countOfRecordedBloodPressures = bloodPressureRepository.bloodPressureCountImmediate(patientUuid)
            val countOfRecordedBloodSugars = bloodSugarRepository.bloodSugarCountImmediate(patientUuid)
            val medicalHistory = medicalHistoryRepository.historyForPatientOrDefaultImmediate(
                defaultHistoryUuid = uuidGenerator.v4(),
                patientUuid = patientUuid
            )
            val hasPatientMeasurementDataChanged = patientRepository.hasPatientMeasurementDataChangedSince(
                patientUuid = patientUuid,
                timestamp = timestamp
            )
            val hasAppointmentChanged = appointmentRepository.hasAppointmentForPatientChangedSince(
                patientUuid = patientUuid,
                timestamp = loadDataForBackClick.screenCreatedTimestamp
            )
            val prescribedDrugs = prescriptionRepository.newestPrescriptionsForPatientImmediate(patientUuid)

            DataForBackClickLoaded(
                hasPatientMeasurementDataChangedSinceScreenCreated = hasPatientMeasurementDataChanged,
                hasAppointmentChangeSinceScreenCreated = hasAppointmentChanged,
                countOfRecordedBloodPressures = countOfRecordedBloodPressures,
                countOfRecordedBloodSugars = countOfRecordedBloodSugars,
                medicalHistory = medicalHistory,
                canShowPatientReassignmentWarning = loadDataForBackClick.canShowPatientReassignmentWarning,
                prescribedDrugs = prescribedDrugs,
                diagnosisWarningPrescriptions = diagnosisWarningPrescriptions.get()
            )
          }
    }
  }

  private fun loadDataForDoneClick(
      scheduler: Scheduler
  ): ObservableTransformer<LoadDataForDoneClick, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { loadDataForDoneClick ->
            val patientUuid = loadDataForDoneClick.patientUuid
            val countOfRecordedBloodPressures = bloodPressureRepository.bloodPressureCountImmediate(patientUuid)
            val countOfRecordedBloodSugars = bloodSugarRepository.bloodSugarCountImmediate(patientUuid)
            val medicalHistory = medicalHistoryRepository.historyForPatientOrDefaultImmediate(
                defaultHistoryUuid = uuidGenerator.v4(),
                patientUuid = patientUuid
            )
            val hasPatientMeasurementDataChanged = patientRepository.hasPatientMeasurementDataChangedSince(
                patientUuid = patientUuid,
                timestamp = loadDataForDoneClick.screenCreatedTimestamp
            )
            val hasAppointmentChanged = appointmentRepository.hasAppointmentForPatientChangedSince(
                patientUuid = patientUuid,
                timestamp = loadDataForDoneClick.screenCreatedTimestamp
            )
            val prescribedDrugs = prescriptionRepository.newestPrescriptionsForPatientImmediate(patientUuid)

            DataForDoneClickLoaded(
                hasPatientMeasurementDataChangedSinceScreenCreated = hasPatientMeasurementDataChanged,
                hasAppointmentChangeSinceScreenCreated = hasAppointmentChanged,
                countOfRecordedBloodPressures = countOfRecordedBloodPressures,
                countOfRecordedBloodSugars = countOfRecordedBloodSugars,
                medicalHistory = medicalHistory,
                canShowPatientReassignmentWarning = loadDataForDoneClick.canShowPatientReassignmentWarning,
                prescribedDrugs = prescribedDrugs,
                diagnosisWarningPrescriptions = diagnosisWarningPrescriptions.get()
            )
          }
    }
  }

  private fun triggerSync(): ObservableTransformer<TriggerSync, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .doOnNext { dataSync.fireAndForgetSync() }
          .map { SyncTriggered(it.sheetOpenedFrom) }
    }
  }

  private fun hasInvalidPhone(patientUuid: UUID): Boolean {
    val phoneNumber = patientRepository.latestPhoneNumberForPatient(patientUuid)
    val appointment = appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)

    return when {
      !phoneNumber.isPresent || !appointment.isPresent -> false
      else -> {
        val actualNumber = phoneNumber.get()
        val actualAppointment = appointment.get()

        val wasAppointmentUpdatedAfterPhoneNumber = actualAppointment.updatedAt > actualNumber.updatedAt
        actualAppointment.wasCancelledBecauseOfInvalidPhoneNumber() && wasAppointmentUpdatedAfterPhoneNumber
      }
    }
  }

  private fun fetchHasShownMissingPhoneReminder(
      scheduler: Scheduler
  ): ObservableTransformer<FetchHasShownMissingPhoneReminder, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .map { it.patientUuid }
          .map(missingPhoneReminderRepository::hasShownReminderForPatient)
          .map(::FetchedHasShownMissingPhoneReminder)
    }
  }
}
