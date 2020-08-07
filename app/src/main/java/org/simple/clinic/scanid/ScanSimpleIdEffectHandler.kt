package org.simple.clinic.scanid

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class ScanSimpleIdEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val uiActions: ScanSimpleIdUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: ScanSimpleIdUiActions): ScanSimpleIdEffectHandler
  }

  fun build(): ObservableTransformer<ScanSimpleIdEffect, ScanSimpleIdEvent> = RxMobius
      .subtypeEffectHandler<ScanSimpleIdEffect, ScanSimpleIdEvent>()
      .addAction(ShowQrCodeScannerView::class.java, uiActions::showQrCodeScannerView, schedulersProvider.ui())
      .addAction(HideQrCodeScannerView::class.java, uiActions::hideQrCodeScannerView, schedulersProvider.ui())
      .addAction(HideShortCodeValidationError::class.java, uiActions::hideShortCodeValidationError, schedulersProvider.ui())
      .addConsumer(ShowShortCodeValidationError::class.java, { uiActions.showShortCodeValidationError(it.failure) }, schedulersProvider.ui())
      .addTransformer(ValidateShortCode::class.java, validateShortCode())
      .addConsumer(OpenPatientShortCodeSearch::class.java, { uiActions.openPatientShortCodeSearch(it.shortCode) }, schedulersProvider.ui())
      .addConsumer(OpenPatientSummary::class.java, { uiActions.openPatientSummary(it.patientUuid) }, schedulersProvider.ui())
      .addConsumer(OpenAddIdToPatientScreen::class.java, { uiActions.openAddIdToPatientScreen(it.identifier) }, schedulersProvider.ui())
      .addTransformer(SearchPatient::class.java, searchPatient())
      .build()

  private fun searchPatient(): ObservableTransformer<SearchPatient, ScanSimpleIdEvent> {
    return ObservableTransformer { effects ->
      effects
          .switchMap { effect ->
            val scannedUuid = effect.scannedUuid

            patientRepository
                .findPatientWithBusinessId(scannedUuid.toString())
                .subscribeOn(schedulersProvider.io())
                .map { PatientSearchCompleted(it, scannedUuid) }
          }
    }
  }

  private fun validateShortCode(): ObservableTransformer<ValidateShortCode, ScanSimpleIdEvent> {
    return ObservableTransformer { effects ->
      effects
          .map { it.shortCode.validate() }
          .map(::ShortCodeValidated)
    }
  }
}
