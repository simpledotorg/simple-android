package org.simple.clinic.medicalhistory.newentry

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import io.reactivex.rxkotlin.withLatestFrom
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.medicalhistory.MedicalHistoryRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent

typealias Ui = NewMedicalHistoryUi
typealias UiChange = (Ui) -> Unit

class NewMedicalHistoryScreenController @AssistedInject constructor(
    private val medicalHistoryRepository: MedicalHistoryRepository,
    private val patientRepository: PatientRepository,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    @Assisted private val modelSupplier: () -> NewMedicalHistoryModel
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(modelSupplier: () -> NewMedicalHistoryModel): NewMedicalHistoryScreenController
  }

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events).replay()

    return Observable.mergeArray(
        showPatientName(replayedEvents),
        saveMedicalHistoryAndShowSummary(replayedEvents))
  }

  private fun showPatientName(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<ScreenCreated>()
        .flatMapSingle { _ ->
          patientRepository.ongoingEntry()
              .map { it.personalDetails!!.fullName }
              .map { { ui: Ui -> ui.setPatientName(it) } }
        }
  }

  private fun saveMedicalHistoryAndShowSummary(events: Observable<UiEvent>): Observable<UiChange> {
    val currentUserStream = userSession
        .requireLoggedInUser()
        .replay()
        .refCount()

    val currentFacilityStream = currentUserStream
        .flatMap { loggedInUser -> facilityRepository.currentFacility(loggedInUser) }

    return events
        .ofType<SaveMedicalHistoryClicked>()
        .withLatestFrom(currentUserStream, currentFacilityStream) { _, loggedInUser, currentFacility ->
          loggedInUser to currentFacility
        }
        .flatMapSingle { (loggedInUser, currentFacility) -> patientRepository.saveOngoingEntryAsPatient(loggedInUser, currentFacility) }
        .flatMap { savedPatient ->
          medicalHistoryRepository
              .save(savedPatient.uuid, modelSupplier().ongoingMedicalHistoryEntry)
              .andThen(Observable.just({ ui: Ui -> ui.openPatientSummaryScreen(savedPatient.uuid) }))
        }
  }
}
