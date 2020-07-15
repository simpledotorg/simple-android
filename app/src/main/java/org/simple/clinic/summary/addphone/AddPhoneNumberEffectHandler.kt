package org.simple.clinic.summary.addphone

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PhoneNumberDetails
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator

class AddPhoneNumberEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val repository: PatientRepository,
    private val uuidGenerator: UuidGenerator,
    @Assisted private val uiActions: UiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: UiActions): AddPhoneNumberEffectHandler
  }

  fun build(): ObservableTransformer<AddPhoneNumberEffect, AddPhoneNumberEvent> = RxMobius
      .subtypeEffectHandler<AddPhoneNumberEffect, AddPhoneNumberEvent>()
      .addTransformer(AddPhoneNumber::class.java, addPhoneNumber())
      .addAction(CloseDialog::class.java, uiActions::closeDialog, schedulersProvider.ui())
      .build()

  private fun addPhoneNumber(): ObservableTransformer<AddPhoneNumber, AddPhoneNumberEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .flatMap {
            repository.createPhoneNumberForPatient(
                uuid = uuidGenerator.v4(),
                patientUuid = it.patientUuid,
                numberDetails = PhoneNumberDetails.mobile(it.newNumber),
                active = true
            ).andThen(Observable.just(PhoneNumberAdded))
          }
    }
  }
}
