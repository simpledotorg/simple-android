package org.simple.clinic.deeplink

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class DeepLinkEffectHandler @Inject constructor(
    private val userSession: Lazy<UserSession>,
    private val schedulerProvider: SchedulersProvider
) {

  fun build(): ObservableTransformer<DeepLinkEffect, DeepLinkEvent> = RxMobius
      .subtypeEffectHandler<DeepLinkEffect, DeepLinkEvent>()
      .addTransformer(FetchUser::class.java, fetchUser())
      .build()

  private fun fetchUser(): ObservableTransformer<FetchUser, DeepLinkEvent> {
    return ObservableTransformer { effectsStream ->
      effectsStream
          .observeOn(schedulerProvider.io())
          .map {
            val user = userSession.get().loggedInUserImmediate()
            UserFetched(user)
          }
    }
  }
}
