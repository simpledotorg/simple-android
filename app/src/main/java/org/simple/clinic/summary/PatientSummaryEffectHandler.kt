package org.simple.clinic.summary

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.appconfig.Country
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.summary.addphone.MissingPhoneReminderRepository
import org.simple.clinic.summary.teleconsultation.api.TeleconsultInfo
import org.simple.clinic.summary.teleconsultation.api.TeleconsultationApi
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.SyncGroup.FREQUENT
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.util.UUID

class PatientSummaryEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val bloodPressureRepository: BloodPressureRepository,
    private val appointmentRepository: AppointmentRepository,
    private val missingPhoneReminderRepository: MissingPhoneReminderRepository,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val bloodSugarRepository: BloodSugarRepository,
    private val dataSync: DataSync,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val country: Country,
    private val patientSummaryConfig: PatientSummaryConfig,
    private val teleconsultationApi: TeleconsultationApi,
    @Assisted private val uiActions: PatientSummaryUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: PatientSummaryUiActions): PatientSummaryEffectHandler
  }

  fun build(): ObservableTransformer<PatientSummaryEffect, PatientSummaryEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientSummaryEffect, PatientSummaryEvent>()
        .addTransformer(LoadPatientSummaryProfile::class.java, loadPatientSummaryProfile(schedulersProvider.io()))
        .addTransformer(LoadCurrentFacility::class.java, loadCurrentFacility(schedulersProvider.io()))
        .addConsumer(HandleEditClick::class.java, { uiActions.showEditPatientScreen(it.patientSummaryProfile, it.currentFacility) }, schedulersProvider.ui())
        .addAction(HandleLinkIdCancelled::class.java, { uiActions.goToPreviousScreen() }, schedulersProvider.ui())
        .addAction(GoBackToPreviousScreen::class.java, { uiActions.goToPreviousScreen() }, schedulersProvider.ui())
        .addAction(GoToHomeScreen::class.java, { uiActions.goToHomeScreen() }, schedulersProvider.ui())
        .addTransformer(CheckForInvalidPhone::class.java, checkForInvalidPhone(schedulersProvider.io(), schedulersProvider.ui()))
        .addTransformer(MarkReminderAsShown::class.java, markReminderAsShown(schedulersProvider.io()))
        .addConsumer(ShowAddPhonePopup::class.java, { uiActions.showAddPhoneDialog(it.patientUuid) }, schedulersProvider.ui())
        .addTransformer(ShowLinkIdWithPatientView::class.java, showLinkIdWithPatientView(schedulersProvider.ui()))
        .addAction(HideLinkIdWithPatientView::class.java, { uiActions.hideLinkIdWithPatientView() }, schedulersProvider.ui())
        .addConsumer(
            ShowScheduleAppointmentSheet::class.java,
            { uiActions.showScheduleAppointmentSheet(it.patientUuid, it.sheetOpenedFrom, it.currentFacility) },
            schedulersProvider.ui()
        )
        .addTransformer(LoadDataForBackClick::class.java, loadDataForBackClick(schedulersProvider.io()))
        .addTransformer(LoadDataForDoneClick::class.java, loadDataForDoneClick(schedulersProvider.io()))
        .addTransformer(TriggerSync::class.java, triggerSync())
        .addAction(ShowDiagnosisError::class.java, { uiActions.showDiagnosisError() }, schedulersProvider.ui())
        .addTransformer(FetchHasShownMissingPhoneReminder::class.java, fetchHasShownMissingPhoneReminder(schedulersProvider.io()))
        .addConsumer(OpenContactPatientScreen::class.java, { uiActions.openPatientContactSheet(it.patientUuid) }, schedulersProvider.ui())
        .addTransformer(LoadPatientTeleconsultationInfo::class.java, fetchPatientTeleconsulationInfo())
        .addConsumer(ContactDoctor::class.java, { uiActions.contactDoctor(it.patientInformation) }, schedulersProvider.ui())
        .addTransformer(FetchTeleconsultationInfo::class.java, fetchFacilityTeleconsultationInfo())
        .build()
  }

  private fun loadPatientSummaryProfile(scheduler: Scheduler): ObservableTransformer<LoadPatientSummaryProfile, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .switchMap { patientRepository.patientProfile(it.patientUuid) }
          .filterAndUnwrapJust()
          .map { it.withoutDeletedBusinessIds().withoutDeletedPhoneNumbers() }
          .map(::mapPatientProfileToSummaryProfile)
          .map(::PatientSummaryProfileLoaded)
    }
  }

  private fun mapPatientProfileToSummaryProfile(patientProfile: PatientProfile): PatientSummaryProfile {
    return PatientSummaryProfile(
        patient = patientProfile.patient,
        address = patientProfile.address,
        phoneNumber = patientProfile.phoneNumbers.firstOrNull(),
        bpPassport = patientProfile.businessIds.filter { it.identifier.type == BpPassport }.maxBy { it.createdAt },
        alternativeId = patientProfile.businessIds.filter { it.identifier.type == country.alternativeIdentifierType }.maxBy { it.createdAt }
    )
  }

  // TODO(vs): 2020-02-19 Revisit after Mobius migration
  private fun checkForInvalidPhone(
      backgroundWorkScheduler: Scheduler,
      uiWorkScheduler: Scheduler
  ): ObservableTransformer<CheckForInvalidPhone, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(backgroundWorkScheduler)
          .map { it.patientUuid to hasInvalidPhone(it.patientUuid) }
          .observeOn(uiWorkScheduler)
          .doOnNext { (patientUuid, isPhoneInvalid) ->
            if (isPhoneInvalid) {
              uiActions.showUpdatePhoneDialog(patientUuid)
            }
          }
          .map { CompletedCheckForInvalidPhone }
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
            val medicalHistory = medicalHistoryRepository.historyForPatientOrDefaultImmediate(patientUuid)

            DataForBackClickLoaded(
                hasPatientDataChangedSinceScreenCreated = patientRepository.hasPatientDataChangedSince(patientUuid, timestamp),
                countOfRecordedMeasurements = countOfRecordedMeasurements(patientUuid),
                diagnosisRecorded = medicalHistory.diagnosisRecorded
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
          .map { loadDataForBackClick ->
            val patientUuid = loadDataForBackClick.patientUuid
            val medicalHistory = medicalHistoryRepository.historyForPatientOrDefaultImmediate(patientUuid)

            DataForDoneClickLoaded(countOfRecordedMeasurements = countOfRecordedMeasurements(patientUuid), diagnosisRecorded = medicalHistory.diagnosisRecorded)
          }
    }
  }

  private fun showLinkIdWithPatientView(
      scheduler: Scheduler
  ): ObservableTransformer<ShowLinkIdWithPatientView, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .doOnNext { uiActions.showLinkIdWithPatientView(it.patientUuid, it.identifier) }
          .map { LinkIdWithPatientSheetShown }
    }
  }

  private fun loadCurrentFacility(scheduler: Scheduler): ObservableTransformer<LoadCurrentFacility, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(scheduler)
          .flatMap {
            val user = userSession.loggedInUserImmediate()
            requireNotNull(user)

            facilityRepository
                .currentFacility(user)
                .take(1)
          }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun triggerSync(): ObservableTransformer<TriggerSync, PatientSummaryEvent> {
    return ObservableTransformer { effects ->
      effects
          .doOnNext { dataSync.fireAndForgetSync(FREQUENT) }
          .map { SyncTriggered(it.sheetOpenedFrom) }
    }
  }

  private fun countOfRecordedMeasurements(patientUuid: UUID): Int {
    val countOfRecordedBloodPressures = bloodPressureRepository.bloodPressureCountImmediate(patientUuid)
    val countOfRecordedBloodSugars = bloodSugarRepository.bloodSugarCountImmediate(patientUuid)

    return countOfRecordedBloodPressures + countOfRecordedBloodSugars
  }

  private fun hasInvalidPhone(patientUuid: UUID): Boolean {
    val phoneNumber = patientRepository.latestPhoneNumberForPatient(patientUuid)
    val appointment = appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)

    return when {
      phoneNumber.isEmpty() || appointment.isEmpty() -> false
      else -> {
        val actualNumber = (phoneNumber as Just).value
        val actualAppointment = (appointment as Just).value

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

  private fun fetchPatientTeleconsulationInfo(): ObservableTransformer<LoadPatientTeleconsultationInfo, PatientSummaryEvent> {
    return ObservableTransformer { loadPatientInformationStream ->
      loadPatientInformationStream
          .observeOn(schedulersProvider.io())
          .map {
            val bloodPressures = bloodPressureRepository.newestMeasurementsForPatientImmediate(it.patientUuid, patientSummaryConfig.numberOfMeasurementsForTeleconsultation)
            val prescriptions = prescriptionRepository.newestPrescriptionsForPatientImmediate(it.patientUuid)
            PatientTeleconsultationInfo(
                it.patientUuid,
                it.bpPassport?.identifier?.displayValue(),
                it.currentFacility!!,
                bloodPressures,
                prescriptions
            )
          }
          .map(::PatientTeleconsultationInfoLoaded)
    }
  }

  private fun fetchFacilityTeleconsultationInfo(): ObservableTransformer<FetchTeleconsultationInfo, PatientSummaryEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .observeOn(schedulersProvider.io())
          .switchMap { fetchPhoneNumber(it.facilityUuid) }
          .map { teleconsultInfo -> FetchedTeleconsultationInfo(teleconsultInfo) }
    }
  }

  private fun fetchPhoneNumber(facilityUuid: UUID): Observable<TeleconsultInfo> {
    return teleconsultationApi
        .get(facilityUuid)
        .map {
          val phoneNumber = it.teleconsultationPhoneNumber
          if (phoneNumber.isNullOrBlank()) {
            TeleconsultInfo.MissingPhoneNumber
          } else {
            TeleconsultInfo.Fetched(phoneNumber)
          }
        }
        .onErrorReturn { TeleconsultInfo.NetworkError }
        .toObservable()
  }
}
