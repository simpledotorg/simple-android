package org.simple.clinic.summary

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.cast
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.Just
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class PatientSummaryEffectHandler @Inject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository
) {

  fun build(): ObservableTransformer<PatientSummaryEffect, PatientSummaryEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientSummaryEffect, PatientSummaryEvent>()
        .addTransformer(LoadPatientSummaryProfile::class.java) { effects ->
          effects.flatMap { fetchPatientSummaryProfile ->
            val patientUuid = fetchPatientSummaryProfile.patientUuid

            // TODO(vs): 2020-01-10 Simplify this chain once refactoring is done
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
        .build()
  }
}
