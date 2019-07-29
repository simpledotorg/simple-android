package org.simple.clinic.login.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.UiEvent

class UpdateLoginEntryWithEnteredPin(private val userSession: UserSession) : ObservableTransformer<UiEvent, UiEvent> {

  override fun apply(upstream: Observable<UiEvent>): ObservableSource<UiEvent> {
    val enteredPin = upstream
        .ofType<LoginPinAuthenticated>()
        .map { it.pin }

    val updatedEntryStream = enteredPin
        .flatMapSingle(this::currentEntryWithNewPin)
        .flatMapSingle(this::saveUpdatedLoginEntry)
        .map(::LoginPinScreenUpdatedLoginEntry)

    return upstream.mergeWith(updatedEntryStream)
  }

  private fun saveUpdatedLoginEntry(newEntry: OngoingLoginEntry): Single<OngoingLoginEntry> {
    return userSession
        .saveOngoingLoginEntry(newEntry)
        .toSingleDefault(newEntry)
  }

  private fun currentEntryWithNewPin(pin: String): Single<OngoingLoginEntry> {
    return userSession
        .ongoingLoginEntry()
        .map { it.copy(pin = pin) }
  }
}
