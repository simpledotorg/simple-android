package org.simple.clinic.scanid

import com.spotify.mobius.rx2.RxMobius
import com.squareup.moshi.Moshi
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class ScanSimpleIdEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    @Assisted private val uiActions: ScanSimpleIdUiActions,
    private val moshi: Moshi
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: ScanSimpleIdUiActions): ScanSimpleIdEffectHandler
  }

  fun build(): ObservableTransformer<ScanSimpleIdEffect, ScanSimpleIdEvent> = RxMobius
      .subtypeEffectHandler<ScanSimpleIdEffect, ScanSimpleIdEvent>()
      .addAction(ShowQrCodeScannerView::class.java, uiActions::showQrCodeScannerView, schedulersProvider.ui())
      .addAction(HideQrCodeScannerView::class.java, uiActions::hideQrCodeScannerView, schedulersProvider.ui())
      .addAction(HideEnteredCodeValidationError::class.java, uiActions::hideShortCodeValidationError, schedulersProvider.ui())
      .addConsumer(ShowEnteredCodeValidationError::class.java, { uiActions.showShortCodeValidationError(it.failure) }, schedulersProvider.ui())
      .addTransformer(ValidateEnteredCode::class.java, validateShortCode())
      .addTransformer(SearchPatientByIdentifier::class.java, searchPatientByIdentifier())
      .addTransformer(ParseScannedJson::class.java, parseJsonIntoObject())
      .addConsumer(OpenPatientSummary::class.java, ::openPatientSummary, schedulersProvider.ui())
      .addConsumer(OpenShortCodeSearch::class.java, ::openShortCodeSearch, schedulersProvider.ui())
      .addConsumer(OpenPatientSearch::class.java, ::openPatientSearch, schedulersProvider.ui())
      .build()

  private fun openPatientSearch(openPatientSearch: OpenPatientSearch) {
    uiActions.openPatientSearch(openPatientSearch.additionalIdentifier)
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
            val adapter = moshi.adapter(IndiaNHIDInfoPayload::class.java)
            adapter.fromJson(it.text)
          }
          .map { it.fromPayload() }
          .map(::ScannedQRCodeJsonParsed)
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

  private fun validateShortCode(): ObservableTransformer<ValidateEnteredCode, ScanSimpleIdEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { it.enteredCode.validate() }
          .map(::EnteredCodeValidated)
    }
  }
}
