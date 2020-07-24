package org.simple.clinic.enterotp

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.sync.DataSync
import org.simple.clinic.user.OngoingLoginEntryRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.SchedulersProvider

class EnterOtpEffectHandler @AssistedInject constructor(
    private val schedulers: SchedulersProvider,
    private val userSession: UserSession,
    private val dataSync: DataSync,
    private val ongoingLoginEntryRepository: OngoingLoginEntryRepository,
    @Assisted private val uiActions: EnterOtpUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: EnterOtpUiActions): EnterOtpEffectHandler
  }

  fun build(): ObservableTransformer<EnterOtpEffect, EnterOtpEvent> {
    return RxMobius
        .subtypeEffectHandler<EnterOtpEffect, EnterOtpEvent>()
        .addTransformer(LoadUser::class.java, loadUser())
        .addAction(ClearPin::class.java, uiActions::clearPin, schedulers.ui())
        .addAction(TriggerSync::class.java, dataSync::fireAndForgetSync)
        .addAction(ClearLoginEntry::class.java, ongoingLoginEntryRepository::clearLoginEntry)
        .build()
  }

  private fun loadUser(): ObservableTransformer<LoadUser, EnterOtpEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulers.io())
          .map { userSession.loggedInUserImmediate()!! }
          .map(::UserLoaded)
    }
  }
}
