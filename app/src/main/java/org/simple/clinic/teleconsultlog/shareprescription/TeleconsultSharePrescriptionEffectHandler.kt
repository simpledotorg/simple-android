package org.simple.clinic.teleconsultlog.shareprescription

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
import org.simple.clinic.util.Optional
import org.simple.clinic.util.extractIfPresent
import org.simple.clinic.util.scheduler.SchedulersProvider

class TeleconsultSharePrescriptionEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val patientRepository: PatientRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val signatureRepository: SignatureRepository,
    @Assisted private val uiActions: TeleconsultSharePrescriptionUiActions,
    @TypedPreference(MedicalRegistrationId) private val medicalRegistrationId: Preference<Optional<String>>,
    private val teleconsultSharePrescriptionRepository: TeleconsultSharePrescriptionRepository
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: TeleconsultSharePrescriptionUiActions): TeleconsultSharePrescriptionEffectHandler
  }

  fun build(): ObservableTransformer<TeleconsultSharePrescriptionEffect, TeleconsultSharePrescriptionEvent> {
    return RxMobius
        .subtypeEffectHandler<TeleconsultSharePrescriptionEffect, TeleconsultSharePrescriptionEvent>()
        .addTransformer(LoadPatientMedicines::class.java, loadPatientMedicines())
        .addTransformer(LoadSignature::class.java, loadSignature())
        .addConsumer(SetSignature::class.java, { uiActions.setSignatureBitmap(it.bitmap) }, schedulersProvider.ui())
        .addTransformer(LoadMedicalRegistrationId::class.java, loadMedicalRegistrationID())
        .addConsumer(SetMedicalRegistrationId::class.java, { uiActions.setMedicalRegistrationId(it.medicalRegistrationId) }, schedulersProvider.ui())
        .addTransformer(SaveBitmapInExternalStorage::class.java, saveBitmapInExternalStorage())
        .addAction(GoToHomeScreen::class.java, { uiActions.openHomeScreen() }, schedulersProvider.ui())
        .addTransformer(LoadPatientProfile::class.java, loadPatientProfile())
        .addTransformer(SharePrescriptionAsImage::class.java, saveBitmapInExternalStorageForSharing())
        .addTransformer(RetrievePrescriptionImageUri::class.java, loadPrescriptionImageUri())
        .addConsumer(OpenSharingDialog::class.java, { uiActions.sharePrescriptionAsImage(it.imageUri) }, schedulersProvider.ui())
        .addAction(GoBack::class.java, { uiActions.goToPreviousScreen() }, schedulersProvider.ui())
        .addAction(ShowImageSavedToast::class.java, uiActions::showImageSavedToast, schedulersProvider.ui())
        .build()
  }

  private fun loadPrescriptionImageUri(): ObservableTransformer<RetrievePrescriptionImageUri, TeleconsultSharePrescriptionEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { teleconsultSharePrescriptionRepository.sharePrescription(it.fileName) }
          .map(::SharePrescriptionUri)
    }
  }

  private fun saveBitmapInExternalStorageForSharing(): ObservableTransformer<SharePrescriptionAsImage, TeleconsultSharePrescriptionEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { prescriptionAsBitmap ->
            teleconsultSharePrescriptionRepository.savePrescriptionBitmap(prescriptionAsBitmap.bitmap)
          }
          .map(::PrescriptionSavedForSharing)
    }
  }

  private fun loadPatientProfile(): ObservableTransformer<LoadPatientProfile, TeleconsultSharePrescriptionEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { patientRepository.patientProfileImmediate(it.patientUuid) }
          .extractIfPresent()
          .map(::PatientProfileLoaded)
    }
  }

  private fun loadSignature(): ObservableTransformer<LoadSignature, TeleconsultSharePrescriptionEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { signatureRepository.getSignatureBitmap() }
          .map(::SignatureLoaded)
    }
  }

  private fun loadPatientMedicines(): ObservableTransformer<LoadPatientMedicines, TeleconsultSharePrescriptionEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { prescriptionRepository.newestPrescriptionsForPatientImmediate(it.patientUuid) }
          .map(::PatientMedicinesLoaded)
    }
  }

  private fun loadMedicalRegistrationID(): ObservableTransformer<LoadMedicalRegistrationId, TeleconsultSharePrescriptionEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { medicalRegistrationId.get() }
          .extractIfPresent()
          .map(::MedicalRegistrationIdLoaded)
    }
  }

  private fun saveBitmapInExternalStorage(): ObservableTransformer<SaveBitmapInExternalStorage, TeleconsultSharePrescriptionEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { prescriptionAsBitmap ->
            teleconsultSharePrescriptionRepository.savePrescriptionBitmap(prescriptionAsBitmap.bitmap)
          }
          .map { PrescriptionImageSaved }
    }
  }
}
