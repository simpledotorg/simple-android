package org.simple.clinic.scanid

import com.spotify.mobius.rx2.RxMobius
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.appconfig.Country
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class ScanSimpleIdEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val moshi: Moshi,
    private val country: Country,
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
      .addConsumer(OpenShortCodeSearch::class.java, ::openShortCodeSearch, schedulersProvider.ui())
      .addConsumer(OpenPatientSearch::class.java, ::openPatientSearch, schedulersProvider.ui())
      .build()

  private fun openPatientSearch(openPatientSearch: OpenPatientSearch) {
    uiActions.openPatientSearch(openPatientSearch.additionalIdentifier,
        openPatientSearch.initialSearchQuery)
  }

  private fun openShortCodeSearch(openShortCodeSearch: OpenShortCodeSearch) {
    uiActions.openShortCodeSearch(openShortCodeSearch.shortCode)
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
            }
          }
    }
  }

  private fun parseJsonBasedOnCountry(effect: ParseScannedJson): ScanSimpleIdEvent {
    return when (country.isoCountryCode) {
      Country.INDIA -> {
        val adapter = moshi.adapter(IndiaNHIDInfoPayload::class.java)
        val payload = adapter.fromJson(effect.text)

        ScannedQRCodeJsonParsed(payload?.toPatientPrefillInfo(), payload?.healthIdNumber)
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
