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
    @TypedPreference(MedicalRegistrationId) private val medicalRegistrationId: Preference<Optional<String>>
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: TeleconsultSharePrescriptionUiActions): TeleconsultSharePrescriptionEffectHandler
  }

  fun build(): ObservableTransformer<TeleconsultSharePrescriptionEffect, TeleconsultSharePrescriptionEvent> {
    return RxMobius
        .subtypeEffectHandler<TeleconsultSharePrescriptionEffect, TeleconsultSharePrescriptionEvent>()
        .addTransformer(LoadPatientDetails::class.java, loadPatientDetails())
        .addTransformer(LoadPatientMedicines::class.java, loadPatientMedicines())
        .addTransformer(LoadSignature::class.java, loadSignature())
        .addConsumer(SetSignature::class.java, { uiActions.setSignatureBitmap(it.bitmap) }, schedulersProvider.ui())
        .addTransformer(LoadMedicalRegistrationId::class.java, loadMedicalRegistrationID())
        .addConsumer(SetMedicalRegistrationId::class.java, { uiActions.setMedicalRegistrationId(it.medicalRegistrationId) }, schedulersProvider.ui())
        .addConsumer(GoToHomeScreen::class.java, { uiActions.openHomeScreen() }, schedulersProvider.ui())
        .build()
  }

  private fun loadSignature(): ObservableTransformer<LoadSignature, TeleconsultSharePrescriptionEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { signatureRepository.getSignatureBitmap() }
          .map(::SignatureLoaded)
    }
  }

  private fun loadPatientDetails(): ObservableTransformer<LoadPatientDetails, TeleconsultSharePrescriptionEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { patientRepository.patientImmediate(it.patientUuid) }
          .map(::PatientDetailsLoaded)
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
}
