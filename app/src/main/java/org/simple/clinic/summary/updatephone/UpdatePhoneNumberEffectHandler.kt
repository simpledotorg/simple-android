package org.simple.clinic.summary.updatephone

import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.LANDLINE_OR_MOBILE
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.scheduler.SchedulersProvider

class UpdatePhoneNumberEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val validator: PhoneNumberValidator,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: UpdatePhoneNumberUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: UpdatePhoneNumberUiActions): UpdatePhoneNumberEffectHandler
  }

  fun build(): ObservableTransformer<UpdatePhoneNumberEffect, UpdatePhoneNumberEvent> = RxMobius
      .subtypeEffectHandler<UpdatePhoneNumberEffect, UpdatePhoneNumberEvent>()
      .addConsumer(PrefillPhoneNumber::class.java, { uiActions.preFillPhoneNumber(it.phoneNumber) }, schedulersProvider.ui())
      .addTransformer(LoadPhoneNumber::class.java, loadPhoneNumber())
      .addTransformer(ValidatePhoneNumber::class.java, validatePhoneNumber())
      .addAction(ShowBlankPhoneNumberError::class.java, uiActions::showBlankPhoneNumberError, schedulersProvider.ui())
      .addConsumer(ShowPhoneNumberTooShortError::class.java, { uiActions.showPhoneNumberTooShortError(it.minimumAllowedNumberLength) }, schedulersProvider.ui())
      .addConsumer(ShowPhoneNumberTooLongError::class.java, { uiActions.showPhoneNumberTooLongError(it.maximumRequiredNumberLength) }, schedulersProvider.ui())
      .addAction(CloseDialog::class.java, uiActions::closeDialog, schedulersProvider.ui())
      .addTransformer(SaveNewPhoneNumber::class.java, saveNewPhoneNumber())
      .addTransformer(SaveExistingPhoneNumber::class.java, saveExistingPhoneNumber())
      .build()

  /**
   * The dialog is never shown again once it's dismissed, until the phone number
   * is updated again and an appointment is canceled again. In order to identify
   * if the dialog can be shown, the timestamps of the cancelled appointment and
   * the phone number are compared.
   *
   * As a result, it's necessary to always bump the phone number's update
   * timestamp even if it wasn't unchanged.
   */
  private fun saveExistingPhoneNumber(): ObservableTransformer<SaveExistingPhoneNumber, UpdatePhoneNumberEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap { (patientUuid) ->
            patientRepository
                .phoneNumber(patientUuid)
                .extractIfPresent()
                .switchMap { phoneNumber ->
                  patientRepository.updatePhoneNumberForPatient(
                      patientUuid = patientUuid,
                      phoneNumber = phoneNumber
                  ).andThen(Observable.just(ExistingPhoneNumberSaved))
                }
          }
    }
  }

  private fun saveNewPhoneNumber(): ObservableTransformer<SaveNewPhoneNumber, UpdatePhoneNumberEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .switchMap { (patientUuid, newPhoneNumber) ->
            patientRepository
                .phoneNumber(patientUuid)
                .extractIfPresent()
                .switchMap { existingPhone ->
                  patientRepository.updatePhoneNumberForPatient(
                      patientUuid = patientUuid,
                      phoneNumber = existingPhone.copy(number = newPhoneNumber)
                  ).andThen(Observable.just(NewPhoneNumberSaved))
                }
          }
    }
  }

  private fun validatePhoneNumber(): ObservableTransformer<ValidatePhoneNumber, UpdatePhoneNumberEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { it.phoneNumber to validator.validate(number = it.phoneNumber, type = LANDLINE_OR_MOBILE) }
          .map { (phoneNumber, validationResult) -> PhoneNumberValidated(phoneNumber, validationResult) }
    }
  }

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
