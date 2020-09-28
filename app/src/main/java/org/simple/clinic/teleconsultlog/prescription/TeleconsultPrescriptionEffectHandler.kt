package org.simple.clinic.teleconsultlog.prescription

import com.f2prateek.rx.preferences2.Preference
import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.main.TypedPreference
import org.simple.clinic.main.TypedPreference.Type.MedicalRegistrationId
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.signature.SignatureRepository
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.SchedulersProvider
import java.util.UUID

class TeleconsultPrescriptionEffectHandler @AssistedInject constructor(
    private val patientRepository: PatientRepository,
    private val signatureRepository: SignatureRepository,
    private val teleconsultRecordRepository: TeleconsultRecordRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val schedulersProvider: SchedulersProvider,
    @TypedPreference(MedicalRegistrationId) private val medicalRegistrationIdPreference: Preference<Optional<String>>,
    @Assisted private val uiActions: TeleconsultPrescriptionUiActions
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: TeleconsultPrescriptionUiActions): TeleconsultPrescriptionEffectHandler
  }

  fun build(): ObservableTransformer<TeleconsultPrescriptionEffect, TeleconsultPrescriptionEvent> {
    return RxMobius
        .subtypeEffectHandler<TeleconsultPrescriptionEffect, TeleconsultPrescriptionEvent>()
        .addTransformer(LoadPatientDetails::class.java, loadPatientDetails())
        .addAction(GoBack::class.java, uiActions::goBackToPreviousScreen, schedulersProvider.ui())
        .addAction(ShowSignatureRequiredError::class.java, uiActions::showSignatureRequiredError, schedulersProvider.ui())
        .addTransformer(LoadDataForNextClick::class.java, loadDataForNextClick())
        .addTransformer(AddTeleconsultIdToPrescribedDrugs::class.java, createPrescription())
        .addConsumer(OpenSharePrescriptionScreen::class.java, {
          uiActions.openSharePrescriptionScreen(it.teleconsultRecordId, it.medicalInstructions)
        }, schedulersProvider.ui())
        .addConsumer(SaveMedicalRegistrationId::class.java, {
          medicalRegistrationIdPreference.set(Optional.of(it.medicalRegistrationId))
        }, schedulersProvider.io())
        .addConsumer(UpdateTeleconsultRecordMedicalRegistrationId::class.java, {
          teleconsultRecordRepository.updateMedicalRegistrationId(it.teleconsultRecordId, it.medicalRegistrationId)
        }, schedulersProvider.io())
        .build()
  }

  private fun createPrescription(): ObservableTransformer<AddTeleconsultIdToPrescribedDrugs, TeleconsultPrescriptionEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { updatePrescribedDrugTeleconsultId(it.patientUuid, it.teleconsultRecordId) }
          .map { PrescriptionCreated(it.medicalInstructions) }
    }
  }

  private fun updatePrescribedDrugTeleconsultId(patientUuid: UUID, teleconsultRecordId: UUID) {
    val prescribedDrugs = prescriptionRepository.newestPrescriptionsForPatientImmediate(patientUuid)

    prescriptionRepository.addTeleconsultationIdToDrugs(
        prescribedDrugs = prescribedDrugs,
        teleconsultationId = teleconsultRecordId
    )
  }

  private fun loadDataForNextClick(): ObservableTransformer<LoadDataForNextClick, TeleconsultPrescriptionEvent> {
    return ObservableTransformer { effects ->
      effects
          .subscribeOn(schedulersProvider.io())
          .map {
            val bitmap = signatureRepository.getSignatureBitmap()
            DataForNextClickLoaded(
                medicalInstructions = it.medicalInstructions,
                medicalRegistrationId = it.medicalRegistrationId,
                hasSignatureBitmap = bitmap != null
            )
          }
    }
  }

  private fun loadPatientDetails(): ObservableTransformer<LoadPatientDetails, TeleconsultPrescriptionEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { patientRepository.patientImmediate(it.patientUuid) }
          .map(::PatientDetailsLoaded)
    }
  }
}
