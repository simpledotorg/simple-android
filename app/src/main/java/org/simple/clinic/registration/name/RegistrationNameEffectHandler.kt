package org.simple.clinic.registration.name

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.registration.name.RegistrationNameValidationResult.Blank
import org.simple.clinic.registration.name.RegistrationNameValidationResult.Valid

class RegistrationNameEffectHandler @AssistedInject constructor(
    @Assisted private val viewEffectConsumer: Consumer<RegistrationNameViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(
        viewEffectConsumer: Consumer<RegistrationNameViewEffect>
    ): RegistrationNameEffectHandler
  }

  fun build(): ObservableTransformer<RegistrationNameEffect, RegistrationNameEvent> {
    return RxMobius
        .subtypeEffectHandler<RegistrationNameEffect, RegistrationNameEvent>()
        .addTransformer(ValidateEnteredName::class.java, validateNameEntry())
        .addConsumer(RegistrationNameViewEffect::class.java, viewEffectConsumer::accept)
        .build()
  }

  private fun validateNameEntry(): ObservableTransformer<ValidateEnteredName, RegistrationNameEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { if (it.name.isBlank()) Blank else Valid }
          .map(::NameValidated)
    }
  }
}
