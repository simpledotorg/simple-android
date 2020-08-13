package org.simple.clinic.summary.linkId

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.ReplayUntilScreenIsDestroyed
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.uuid.UuidGenerator
import org.simple.clinic.widgets.UiEvent
import javax.inject.Inject

typealias Ui = LinkIdWithPatientViewUi
typealias UiChange = (Ui) -> Unit

class LinkIdWithPatientViewController @Inject constructor(
    private val patientRepository: PatientRepository,
    private val userSession: UserSession,
    private val uuidGenerator: UuidGenerator
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedEvents = ReplayUntilScreenIsDestroyed(events)
        .replay()

    return Observable.never()
  }
}
