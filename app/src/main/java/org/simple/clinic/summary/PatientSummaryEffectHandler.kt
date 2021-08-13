package org.simple.clinic.summary

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
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.summary.addphone.MissingPhoneReminderRepository
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityRepository
import org.simple.clinic.sync.DataSync
import org.simple.clinic.user.User
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.util.toNullable
import org.simple.clinic.uuid.UuidGenerator
import java.util.Optional
import java.util.UUID
import java.util.function.Function

class PatientSummaryEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val bloodPressureRepository: BloodPressureRepository,
    private val appointmentRepository: AppointmentRepository,
    private val missingPhoneReminderRepository: MissingPhoneReminderRepository,
    private val bloodSugarRepository: BloodSugarRepository,
    private val dataSync: DataSync,
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val country: Country,
    private val patientSummaryConfig: PatientSummaryConfig,
    private val currentUser: Lazy<User>,
    private val currentFacility: Lazy<Facility>,
    private val uuidGenerator: UuidGenerator,
    private val facilityRepository: FacilityRepository,
    private val teleconsultationFacilityRepository: TeleconsultationFacilityRepository,
    @Assisted private val uiActions: PatientSummaryUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: PatientSummaryUiActions): PatientSummaryEffectHandler
  }

  fun build(): ObservableTransformer<PatientSummaryEffect, PatientSummaryEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientSummaryEffect, PatientSummaryEvent>()
        .addTransformer(LoadPatientSummaryProfile::class.java, loadPatientSummaryProfile(schedulersProvider.io()))
        .addTransformer(LoadCurrentUserAndFacility::class.java, loadUserAndCurrentFacility())
        .addConsumer(HandleEditClick::class.java, { uiActions.showEditPatientScreen(it.patientSummaryProfile, it.currentFacility) }, schedulersProvider.ui())
        .addAction(GoBackToPreviousScreen::class.java, { uiActions.goToPreviousScreen() }, schedulersProvider.ui())
        .addAction(GoToHomeScreen::class.java, { uiActions.goToHomeScreen() }, schedulersProvider.ui())
        .addTransformer(CheckForInvalidPhone::class.java, checkForInvalidPhone(schedulersProvider.io(), schedulersProvider.ui()))
        .addTransformer(MarkReminderAsShown::class.java, markReminderAsShown(schedulersProvider.io()))
        .addConsumer(ShowAddPhonePopup::class.java, { uiActions.showAddPhoneDialog(it.patientUuid) }, schedulersProvider.ui())
        .addConsumer(ShowLinkIdWithPatientView::class.java, { uiActions.showLinkIdWithPatientView(it.patientUuid, it.identifier) }, schedulersProvider.ui())
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
        .addConsumer(NavigateToTeleconsultRecordScreen::class.java, { uiActions.navigateToTeleconsultRecordScreen(it.patientUuid, it.teleconsultRecordId) }, schedulersProvider.ui())
        .addTransformer(LoadMedicalOfficers::class.java, loadMedicalOfficers())
        .addConsumer(OpenContactDoctorSheet::class.java, { uiActions.openContactDoctorSheet(it.patientUuid) }, schedulersProvider.ui())
        .addAction(ShowAddMeasurementsWarningDialog::class.java, uiActions::showAddMeasurementsWarningDialog, schedulersProvider.ui())
        .addAction(ShowAddBloodPressureWarningDialog::class.java, uiActions::showAddBloodPressureWarningDialog, schedulersProvider.ui())
        .addAction(ShowAddBloodSugarWarningDialog::class.java, uiActions::showAddBloodSugarWarningDialog, schedulersProvider.ui())
        .build()
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
          .filterAndUnwrapJust()
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
        facility = facility.toNullable()
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
            val countOfRecordedBloodPressures = bloodPressureRepository.bloodPressureCountImmediate(patientUuid)
            val countOfRecordedBloodSugars = bloodSugarRepository.bloodSugarCountImmediate(patientUuid)
            val medicalHistory = medicalHistoryRepository.historyForPatientOrDefaultImmediate(
                defaultHistoryUuid = uuidGenerator.v4(),
                patientUuid = patientUuid
            )

            DataForBackClickLoaded(
                hasPatientDataChangedSinceScreenCreated = patientRepository.hasPatientDataChangedSince(patientUuid, timestamp),
                countOfRecordedBloodPressures = countOfRecordedBloodPressures,
                countOfRecordedBloodSugars = countOfRecordedBloodSugars,
                medicalHistory = medicalHistory
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
            val countOfRecordedBloodPressures = bloodPressureRepository.bloodPressureCountImmediate(patientUuid)
            val countOfRecordedBloodSugars = bloodSugarRepository.bloodSugarCountImmediate(patientUuid)
            val medicalHistory = medicalHistoryRepository.historyForPatientOrDefaultImmediate(
                defaultHistoryUuid = uuidGenerator.v4(),
                patientUuid = patientUuid
            )

            DataForDoneClickLoaded(
                countOfRecordedBloodPressures = countOfRecordedBloodPressures,
                countOfRecordedBloodSugars = countOfRecordedBloodSugars,
                medicalHistory = medicalHistory
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

  private fun countOfRecordedMeasurements(patientUuid: UUID): Int {
    val countOfRecordedBloodPressures = bloodPressureRepository.bloodPressureCountImmediate(patientUuid)
    val countOfRecordedBloodSugars = bloodSugarRepository.bloodSugarCountImmediate(patientUuid)

    return countOfRecordedBloodPressures + countOfRecordedBloodSugars
  }

  private fun hasInvalidPhone(patientUuid: UUID): Boolean {
    val phoneNumber = patientRepository.latestPhoneNumberForPatient(patientUuid)
    val appointment = appointmentRepository.lastCreatedAppointmentForPatient(patientUuid)

    return when {
      !phoneNumber.isPresent() || !appointment.isPresent() -> false
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
