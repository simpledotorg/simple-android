package org.simple.clinic.registration.phone

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import org.simple.clinic.user.OngoingRegistrationEntry
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.SchedulersProvider

class RegistrationPhoneEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: RegistrationPhoneUiActions,
    private val schedulers: SchedulersProvider,
    private val userSession: UserSession
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: RegistrationPhoneUiActions): RegistrationPhoneEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationPhoneEffect, RegistrationPhoneEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationPhoneEffect, RegistrationPhoneEvent>()
        .addConsumer(PrefillFields::class.java, { uiActions.preFillUserDetails(it.entry) }, schedulers.ui())
        .addTransformer(LoadCurrentRegistrationEntry::class.java, loadCurrentRegistrationEntry())
        .build()
  }

  private fun loadCurrentRegistrationEntry(): ObservableTransformer<LoadCurrentRegistrationEntry, RegistrationPhoneEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMapSingle { currentOngoingRegistrationEntry() }
          .map(::CurrentRegistrationEntryLoaded)
    }
  }

  private fun currentOngoingRegistrationEntry(): Single<Optional<OngoingRegistrationEntry>> {
    return userSession
        .isOngoingRegistrationEntryPresent()
        .flatMap { isRegistrationEntryPresent ->
          // TODO (vs) 04/06/20: This is nasty, make it a synchronous call
          if (isRegistrationEntryPresent)
            userSession
                .ongoingRegistrationEntry()
                .map { Just(it) }
          else
            Single.just(None<OngoingRegistrationEntry>())
        }
  }
}
