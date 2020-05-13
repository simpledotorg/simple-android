package org.simple.clinic.deeplink

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.ObservableTransformer
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider

class DeepLinkEffectHandler @AssistedInject constructor(
    private val userSession: Lazy<UserSession>,
    private val schedulerProvider: SchedulersProvider,
    @Assisted private val uiActions: DeepLinkUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: DeepLinkUiActions): DeepLinkEffectHandler
  }

  fun build(): ObservableTransformer<DeepLinkEffect, DeepLinkEvent> = RxMobius
      .subtypeEffectHandler<DeepLinkEffect, DeepLinkEvent>()
      .addTransformer(FetchUser::class.java, fetchUser())
      .addAction(NavigateToSetupActivity::class.java, { uiActions.navigateToSetupActivity() }, schedulerProvider.ui())
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
