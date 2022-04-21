package org.simple.clinic.forgotpin.createnewpin

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.SECURITY_PIN_LENGTH
import org.simple.clinic.facility.Facility
import org.simple.clinic.user.User
import org.simple.clinic.util.scheduler.SchedulersProvider

class ForgotPinCreateNewEffectHandler @AssistedInject constructor(
    private val currentUser: Lazy<User>,
    private val currentFacility: Lazy<Facility>,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<ForgotPinCreateNewViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectsConsumer: Consumer<ForgotPinCreateNewViewEffect>
    ): ForgotPinCreateNewEffectHandler
  }

  fun build(): ObservableTransformer<ForgotPinCreateNewEffect, ForgotPinCreateNewEvent> = RxMobius
      .subtypeEffectHandler<ForgotPinCreateNewEffect, ForgotPinCreateNewEvent>()
      .addTransformer(LoadLoggedInUser::class.java, loadLoggedInUser())
      .addTransformer(LoadCurrentFacility::class.java, loadFacility())
      .addTransformer(ValidatePin::class.java, validatePin())
      .addConsumer(ForgotPinCreateNewViewEffect::class.java, viewEffectsConsumer::accept)
      .build()

  private fun validatePin(): ObservableTransformer<ValidatePin, ForgotPinCreateNewEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { (pin) ->
            val isPinValid = pin?.length == SECURITY_PIN_LENGTH
            PinValidated(isPinValid)
          }
    }
  }

  private fun loadFacility(): ObservableTransformer<LoadCurrentFacility, ForgotPinCreateNewEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { currentFacility.get() }
          .map(::CurrentFacilityLoaded)
    }
  }

  private fun loadLoggedInUser(): ObservableTransformer<LoadLoggedInUser, ForgotPinCreateNewEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { LoggedInUserLoaded(currentUser.get()) }
    }
  }
}
