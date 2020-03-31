package org.simple.clinic.login.pin

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.mapType
import org.simple.clinic.widgets.UiEvent

class UpdateLoginEntryWithEnteredPin(private val userSession: UserSession) : ObservableTransformer<UiEvent, UiEvent> {

  override fun apply(upstream: Observable<UiEvent>): ObservableSource<UiEvent> {
    val updatedEntryStream = upstream
        .mapType<LoginPinAuthenticated, OngoingLoginEntry> { it.newLoginEntry }
        .flatMapSingle(::saveUpdatedLoginEntry)
        .map(::LoginPinScreenUpdatedLoginEntry)

    return upstream.mergeWith(updatedEntryStream)
  }

  private fun saveUpdatedLoginEntry(newEntry: OngoingLoginEntry): Single<OngoingLoginEntry> {
    return userSession
        .saveOngoingLoginEntry(newEntry)
        .toSingleDefault(newEntry)
  }
}
