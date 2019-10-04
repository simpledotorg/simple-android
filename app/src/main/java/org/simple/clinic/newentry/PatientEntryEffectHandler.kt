package org.simple.clinic.newentry

import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Singles
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider

object PatientEntryEffectHandler {
  fun createEffectHandler(
      userSession: UserSession,
      facilityRepository: FacilityRepository,
      patientRepository: PatientRepository,
      ui: PatientEntryUi,
      schedulersProvider: SchedulersProvider
  ): ObservableTransformer<PatientEntryEffect, PatientEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<PatientEntryEffect, PatientEntryEvent>()
        .addTransformer(FetchPatientEntry::class.java, fetchOngoingEntryEffectHandler(userSession, facilityRepository, patientRepository, schedulersProvider.io()))
        .addConsumer(PrefillFields::class.java, { ui.preFillFields(it.patientEntry) }, schedulersProvider.ui())
        .addAction(ScrollFormToBottom::class.java, { ui.scrollFormToBottom() }, schedulersProvider.ui())
        .addConsumer(ShowEmptyFullNameError::class.java, { ui.showEmptyFullNameError(it.show) }, schedulersProvider.ui())
        .addAction(HidePhoneLengthErrors::class.java, { hidePhoneLengthErrors(ui) }, schedulersProvider.ui())
        .addAction(HideDateOfBirthErrors::class.java, { hideDateOfBirthErrors(ui) }, schedulersProvider.ui())
        .build()
  }

  private fun fetchOngoingEntryEffectHandler(
      userSession: UserSession,
      facilityRepository: FacilityRepository,
      patientRepository: PatientRepository,
      scheduler: Scheduler
  ): ObservableTransformer<FetchPatientEntry, PatientEntryEvent> {
    return ObservableTransformer { fetchPatientEntries ->
      val getPatientEntryAndFacility = Singles.zip(
          patientRepository.ongoingEntry(),
          facilityRepository.currentFacility(userSession).firstOrError()
      )

      fetchPatientEntries
          .flatMapSingle { getPatientEntryAndFacility }
          .subscribeOn(scheduler)
          .map { (entry, facility) ->
            // TODO(rj): 2019-10-03 Extract as function!
            entry.takeIf { it.address != null }
                ?: entry.copy(address = OngoingNewPatientEntry.Address(colonyOrVillage = "", district = facility.district, state = facility.state))
          }
          .map { OngoingEntryFetched(it) }
    }
  }

  private fun hidePhoneLengthErrors(ui: PatientEntryUi) {
    with(ui) {
      showLengthTooLongPhoneNumberError(false)
      showLengthTooShortPhoneNumberError(false)
    }
  }

  private fun hideDateOfBirthErrors(ui: PatientEntryUi) {
    with(ui) {
      showEmptyDateOfBirthAndAgeError(false)
      showInvalidDateOfBirthError(false)
      showDateOfBirthIsInFutureError(false)
    }
  }
}
