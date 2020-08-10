package org.simple.clinic.main

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.time.Instant
import javax.inject.Named

class TheActivityEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val userSession: UserSession,
    private val utcClock: UtcClock,
    @Named("should_lock_after") private val lockAfterTimestamp: Preference<Instant>,
    @Assisted private val uiActions: TheActivityUiActions
) {

  @AssistedInject.Factory
  interface InjectionFactory {
    fun create(uiActions: TheActivityUiActions): TheActivityEffectHandler
  }

  fun build(): ObservableTransformer<TheActivityEffect, TheActivityEvent> {
    return RxMobius
        .subtypeEffectHandler<TheActivityEffect, TheActivityEvent>()
        .addTransformer(LoadAppLockInfo::class.java, loadShowAppLockInto())
        .build()
  }

  private fun loadShowAppLockInto(): ObservableTransformer<LoadAppLockInfo, TheActivityEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap {
            userSession
                .loggedInUser()
                .subscribeOn(schedulers.io())
                .map {
                  AppLockInfoLoaded(
                      user = it,
                      currentTimestamp = Instant.now(utcClock),
                      lockAtTimestamp = lockAfterTimestamp.get()
                  )
                }
          }
    }
  }
}
