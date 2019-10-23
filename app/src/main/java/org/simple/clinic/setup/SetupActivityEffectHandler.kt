package org.simple.clinic.setup

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.Single
import org.simple.clinic.util.scheduler.SchedulersProvider

object SetupActivityEffectHandler {

  fun create(
      onboardingCompletePreference: Preference<Boolean>,
      schedulersProvider: SchedulersProvider
  ): ObservableTransformer<SetupActivityEffect, SetupActivityEvent> {
    return RxMobius
        .subtypeEffectHandler<SetupActivityEffect, SetupActivityEvent>()
        .addTransformer(FetchUserDetails::class.java, fetchUserDetails(onboardingCompletePreference, schedulersProvider.io()))
        .build()
  }

  private fun fetchUserDetails(
      onboardingCompletePreference: Preference<Boolean>,
      scheduler: Scheduler
  ): ObservableTransformer<FetchUserDetails, SetupActivityEvent> {
    return ObservableTransformer { effectStream ->
      effectStream
          .flatMapSingle { Single.just(onboardingCompletePreference.get()) }
          .subscribeOn(scheduler)
          .map(::UserDetailsFetched)
    }
  }
}
