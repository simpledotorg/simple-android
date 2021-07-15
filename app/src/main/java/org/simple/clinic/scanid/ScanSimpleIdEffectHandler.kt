package org.simple.clinic.scanid

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
    @Assisted private val uiActions: ScanSimpleIdUiActions
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: ScanSimpleIdUiActions): ScanSimpleIdEffectHandler
  }

  fun build(): ObservableTransformer<ScanSimpleIdEffect, ScanSimpleIdEvent> = RxMobius
      .subtypeEffectHandler<ScanSimpleIdEffect, ScanSimpleIdEvent>()
      .addAction(ShowQrCodeScannerView::class.java, uiActions::showQrCodeScannerView, schedulersProvider.ui())
      .addAction(HideQrCodeScannerView::class.java, uiActions::hideQrCodeScannerView, schedulersProvider.ui())
      .addAction(HideEnteredCodeValidationError::class.java, uiActions::hideEnteredCodeValidationError, schedulersProvider.ui())
      .addConsumer(ShowEnteredCodeValidationError::class.java, { uiActions.showEnteredCodeValidationError(it.failure) }, schedulersProvider.ui())
      .addTransformer(ValidateEnteredCode::class.java, validateEnteredCode())
      .addTransformer(SearchPatientByIdentifier::class.java, searchPatientByIdentifier())
      .addTransformer(ParseScannedJson::class.java, parseJsonIntoObject())
      .addConsumer(OpenPatientSummary::class.java, ::openPatientSummary, schedulersProvider.ui())
      .addConsumer(OpenPatientSearch::class.java, ::openPatientSearch, schedulersProvider.ui())
      .addTransformer(OnlinePatientLookupWithIdentifier::class.java, onlinePatientLookupWithIdentifier())
      .build()

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

  private fun openPatientSearch(openPatientSearch: OpenPatientSearch) {
    uiActions.openPatientSearch(openPatientSearch.additionalIdentifier,
        openPatientSearch.initialSearchQuery,
        openPatientSearch.patientPrefillInfo)
  }

  private fun openPatientSummary(openPatientSummary: OpenPatientSummary) {
    uiActions.openPatientSummary(openPatientSummary.patientId)
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
