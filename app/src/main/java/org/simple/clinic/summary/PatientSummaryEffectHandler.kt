package org.simple.clinic.summary

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.cast
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.Just
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.util.UUID

class PatientSummaryEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val bloodPressureRepository: BloodPressureRepository,
    @Assisted private val uiActions: PatientSummaryUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: PatientSummaryUiActions): PatientSummaryEffectHandler
  }

  fun build(): ObservableTransformer<PatientSummaryEffect, PatientSummaryEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientSummaryEffect, PatientSummaryEvent>()
        .addTransformer(LoadPatientSummaryProfile::class.java) { effects ->
          // TODO(vs): 2020-01-15 Revisit after Mobius migration
          effects.flatMap { fetchPatientSummaryProfile ->
            val patientUuid = fetchPatientSummaryProfile.patientUuid

            val scheduler = schedulersProvider.io()

            val sharedPatients = patientRepository.patient(patientUuid)
                .subscribeOn(scheduler)
                .map {
                  // We do not expect the patient to get deleted while this screen is already open.
                  (it as Just).value
                }
                .replay(1)
                .refCount()

            val addresses = sharedPatients
                .flatMap { patient -> patientRepository.address(patient.addressUuid).subscribeOn(scheduler) }
                .map { (it as Just).value }

            val latestPhoneNumberStream = patientRepository.phoneNumber(patientUuid).subscribeOn(scheduler)
            val latestBpPassportStream = patientRepository.bpPassportForPatient(patientUuid).subscribeOn(scheduler)

            Observables
                .combineLatest(sharedPatients, addresses, latestPhoneNumberStream, latestBpPassportStream) { patient, address, phoneNumber, bpPassport ->
                  PatientSummaryProfile(patient, address, phoneNumber.toNullable(), bpPassport.toNullable())
                }
                .take(1)
                .map(::PatientSummaryProfileLoaded)
                .cast<PatientSummaryEvent>()
          }
        }
        .addTransformer(HandleBackClick::class.java) { effects ->
          // TODO(vs): 2020-01-15 Revisit after Mobius migration
          effects
              .observeOn(schedulersProvider.io())
              .map { handleBackClick ->

                val (patientUuid, screenCreatedTime) = handleBackClick

                val hasPatientDataChangedSinceScreenCreated = patientRepository.hasPatientDataChangedSince(patientUuid, screenCreatedTime)
                val noBloodPressuresRecordedForPatient = doesNotHaveBloodPressures(patientUuid)

                val shouldShowScheduleAppointmentSheet = if (noBloodPressuresRecordedForPatient) false else hasPatientDataChangedSinceScreenCreated

                shouldShowScheduleAppointmentSheet to patientUuid
              }
              .observeOn(schedulersProvider.ui())
              .doOnNext { (showScheduleAppointmentSheet, patientUuid) ->
                if (showScheduleAppointmentSheet) {
                  uiActions.showScheduleAppointmentSheet(patientUuid)
                }
              }
              .flatMap { Observable.empty<PatientSummaryEvent>() }
        }
        .build()
  }

  private fun doesNotHaveBloodPressures(patientUuid: UUID): Boolean {
    return bloodPressureRepository.bloodPressureCount(patientUuid) == 0
  }
}
