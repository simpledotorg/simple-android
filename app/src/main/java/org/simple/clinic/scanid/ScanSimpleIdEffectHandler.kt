package org.simple.clinic.scanid

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.appconfig.Country
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.onlinelookup.api.LookupPatientOnline
import org.simple.clinic.util.scheduler.SchedulersProvider

class ScanSimpleIdEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val qrCodeJsonParser: QRCodeJsonParser,
    private val country: Country,
    private val lookupPatientOnline: LookupPatientOnline,
    @Assisted private val viewEffectsConsumer: Consumer<ScanSimpleIdViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(viewEffectsConsumer: Consumer<ScanSimpleIdViewEffect>): ScanSimpleIdEffectHandler
  }

  fun build(): ObservableTransformer<ScanSimpleIdEffect, ScanSimpleIdEvent> = RxMobius
      .subtypeEffectHandler<ScanSimpleIdEffect, ScanSimpleIdEvent>()
      .addConsumer(ScanSimpleIdViewEffect::class.java, viewEffectsConsumer::accept, schedulersProvider.ui())
      .addTransformer(ValidateEnteredCode::class.java, validateEnteredCode())
      .addTransformer(SearchPatientByIdentifier::class.java, searchPatientByIdentifier())
      .addTransformer(ParseScannedJson::class.java, parseJsonIntoObject())
      .addTransformer(OnlinePatientLookupWithIdentifier::class.java, onlinePatientLookupWithIdentifier())
      .addTransformer(SaveCompleteMedicalRecords::class.java, saveCompleteMedicalRecords())
      .build()

  private fun saveCompleteMedicalRecords(): ObservableTransformer<SaveCompleteMedicalRecords, ScanSimpleIdEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext {
            patientRepository.saveCompleteMedicalRecord(it.completeMedicalRecords)
          }
          .map { CompleteMedicalRecordsSaved(it.completeMedicalRecords) }
    }
  }

  private fun onlinePatientLookupWithIdentifier(): ObservableTransformer<OnlinePatientLookupWithIdentifier, ScanSimpleIdEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map {
            val results = lookupPatientOnline.lookupWithIdentifier(it.identifier.value)
            OnlinePatientLookupWithIdentifierCompleted(results, it.identifier)
          }
    }
  }

  private fun parseJsonIntoObject(): ObservableTransformer<ParseScannedJson, ScanSimpleIdEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map {
            try {
              parseJsonBasedOnCountry(it)
            } catch (e: JsonDataException) {
              InvalidQrCode
            } catch (e: JsonEncodingException) {
              InvalidQrCode
            }
          }
    }
  }

  private fun parseJsonBasedOnCountry(effect: ParseScannedJson): ScanSimpleIdEvent {
    return when (country.isoCountryCode) {
      Country.INDIA -> {
        val payload = qrCodeJsonParser.parseQRCodeJson(effect.text)
        val healthIdNumber = payload?.healthIdNumber?.filter { it.isDigit() }

        ScannedQRCodeJsonParsed(payload?.toPatientPrefillInfo(), healthIdNumber)
      }
      else -> InvalidQrCode
    }
  }

  private fun searchPatientByIdentifier(): ObservableTransformer<SearchPatientByIdentifier, ScanSimpleIdEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map(SearchPatientByIdentifier::identifier)
          .map { identifier ->
            identifier to patientRepository.findPatientsWithBusinessId(identifier = identifier.value)
          }
          .map { (identifier, patients) ->
            PatientSearchByIdentifierCompleted(patients, identifier)
          }
    }
  }

  private fun validateEnteredCode(): ObservableTransformer<ValidateEnteredCode, ScanSimpleIdEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { it.enteredCode.validate() }
          .map(::EnteredCodeValidated)
    }
  }
}
