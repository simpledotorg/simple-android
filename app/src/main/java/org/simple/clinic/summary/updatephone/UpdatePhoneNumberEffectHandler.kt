package org.simple.clinic.summary.updatephone

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.scheduler.SchedulersProvider

class UpdatePhoneNumberEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: UpdatePhoneNumberUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UpdatePhoneNumberUiActions): UpdatePhoneNumberEffectHandler
  }

  fun build(): ObservableTransformer<UpdatePhoneNumberEffect, UpdatePhoneNumberEvent> = RxMobius
      .subtypeEffectHandler<UpdatePhoneNumberEffect, UpdatePhoneNumberEvent>()
      .addConsumer(PrefillPhoneNumber::class.java, { uiActions.preFillPhoneNumber(it.phoneNumber) }, schedulersProvider.ui())
      .addTransformer(LoadPhoneNumber::class.java, loadPhoneNumber())
      .build()

  private fun loadPhoneNumber(): ObservableTransformer<LoadPhoneNumber, UpdatePhoneNumberEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap { patientRepository.phoneNumber(it.patientUuid) }
          .extractIfPresent()
          .map { PhoneNumberLoaded(it.number) }
    }
  }
}
