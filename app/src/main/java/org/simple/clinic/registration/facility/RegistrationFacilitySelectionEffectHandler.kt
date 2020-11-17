package org.simple.clinic.registration.facility

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.Instant

class RegistrationFacilitySelectionEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val userSession: UserSession,
    private val utcClock: UtcClock,
    @Assisted private val uiActions: RegistrationFacilitySelectionUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: RegistrationFacilitySelectionUiActions): RegistrationFacilitySelectionEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationFacilitySelectionEffect, RegistrationFacilitySelectionEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationFacilitySelectionEffect, RegistrationFacilitySelectionEvent>()
        .addConsumer(OpenConfirmFacilitySheet::class.java, { uiActions.showConfirmFacilitySheet(it.facility.uuid, it.facility.name) }, schedulersProvider.ui())
        .addTransformer(SaveRegistrationEntryAsUser::class.java, saveCurrentRegistrationEntry())
        .addConsumer(MoveToIntroVideoScreen::class.java, { uiActions.openIntroVideoScreen(it.registrationEntry) }, schedulersProvider.ui())
        .build()
  }

  private fun saveCurrentRegistrationEntry(): ObservableTransformer<SaveRegistrationEntryAsUser, RegistrationFacilitySelectionEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            userSession
                .saveOngoingRegistrationEntryAsUser(it.entry, Instant.now(utcClock))
                .andThen(Observable.just(CurrentRegistrationEntrySaved))
          }
    }
  }
}
