package org.simple.clinic.editpatient

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.Just
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = PatientEditScreen
typealias UiChange = (Ui) -> Unit

class PatientEditScreenController @Inject constructor(
    private val patientRepository: PatientRepository
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return prefillOnStart(replayedEvents)
  }

  private fun prefillOnStart(events: Observable<UiEvent>): Observable<UiChange> {

    val patientUuidStream = events.ofType<PatientEditScreenCreated>()
        .map { it.patientUuid }

    val savedPatient = patientUuidStream
        .flatMap(patientRepository::patient)
        .take(1)
        .map { it as Just }
        .map { it.value }
        .cache()

    val savedAddress = savedPatient
        .flatMap { patient ->
          patientRepository
              .address(patient.addressUuid)
              .take(1)
              .map { it as Just }
              .map { it.value }
        }

    val savedPhoneNumber = savedPatient
        .flatMap { patientRepository.phoneNumbers(it.uuid) }
        .take(1)

    return Observables.combineLatest(savedPatient, savedAddress, savedPhoneNumber)
        .map { (patient, address, phoneNumber) ->
          { ui: Ui ->
            ui.setPatientName(patient.fullName)
            ui.setGender(patient.gender)
            ui.setState(address.state)
            ui.setDistrict(address.district)

            if(address.colonyOrVillage.isNullOrBlank().not()) {
              ui.setColonyOrVillage(address.colonyOrVillage!!)
            }

            if(phoneNumber is Just) {
              ui.setPatientPhoneNumber(phoneNumber.value.number)
            }
          }
        }
  }
}
