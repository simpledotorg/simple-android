package org.simple.clinic.drugs.selection.entry

import com.spotify.mobius.rx2.RxMobius
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.nullIfBlank
import org.simple.clinic.util.scheduler.SchedulersProvider

class CustomPrescriptionEntryEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: CustomPrescriptionEntryUiActions,
    private val schedulersProvider: SchedulersProvider,
    private val userSession: UserSession,
    private val facilityRepository: FacilityRepository,
    private val prescriptionRepository: PrescriptionRepository
) {

  @AssistedInject.Factory
  interface Factory {
    fun create(uiActions: CustomPrescriptionEntryUiActions): CustomPrescriptionEntryEffectHandler
  }

  fun build()
      : ObservableTransformer<CustomPrescriptionEntryEffect, CustomPrescriptionEntryEvent> {
    return RxMobius
        .subtypeEffectHandler<CustomPrescriptionEntryEffect, CustomPrescriptionEntryEvent>()
        .addTransformer(SaveCustomPrescription::class.java, saveNewPrescription(schedulersProvider.io()))
        .addTransformer(UpdatePrescription::class.java, updatePrescription(schedulersProvider.io()))
        .addTransformer(FetchPrescription::class.java, fetchPrescription(schedulersProvider.io()))
        .addConsumer(SetMedicineName::class.java, { uiActions.setMedicineName(it.drugName) }, schedulersProvider.ui())
        .addConsumer(SetDosage::class.java, { uiActions.setDosage(it.dosage) }, schedulersProvider.ui())
        .addConsumer(
            ShowConfirmRemoveMedicineDialog::class.java,
            { uiActions.showConfirmRemoveMedicineDialog(it.prescriptionUuid) },
            schedulersProvider.ui()
        )
        .addAction(CloseSheet::class.java, uiActions::finish, schedulersProvider.ui())
        .build()
  }

  private fun updatePrescription(io: Scheduler): ObservableTransformer<UpdatePrescription, CustomPrescriptionEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(io)
          .flatMap { effect ->
            val prescription = prescriptionRepository.prescriptionImmediate(effect.prescriptionUuid)
            prescriptionRepository
                .updatePrescription(prescription.copy(name = effect.drugName, dosage = effect.dosage))
                .andThen(Observable.just(CustomPrescriptionSaved))
          }
    }
  }

  private fun saveNewPrescription(io: Scheduler): ObservableTransformer<SaveCustomPrescription, CustomPrescriptionEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(io)
          .flatMap { savePrescription ->
            val user = userSession.loggedInUserImmediate()!!
            val currentFacility = facilityRepository.currentFacilityImmediate(user)!!

            prescriptionRepository
                .savePrescription(
                    patientUuid = savePrescription.patientUuid,
                    name = savePrescription.drugName,
                    dosage = savePrescription.dosage.nullIfBlank(),
                    rxNormCode = null,
                    isProtocolDrug = false,
                    facility = currentFacility
                )
                .andThen(Observable.just(CustomPrescriptionSaved))
          }
    }
  }

  private fun fetchPrescription(io: Scheduler): ObservableTransformer<FetchPrescription, CustomPrescriptionEntryEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(io)
          // FIXME: The logic to close sheet after a prescription is deleted is dependant on this prescription stream
          //  being reactive. So we have to use the deprecated method here. This should be fixed.
          //  This is being tracked here: https://www.pivotaltracker.com/story/show/172737790
          .flatMap { prescriptionRepository.prescription(it.prescriptionUuid) }
          .map(::CustomPrescriptionFetched)
    }
  }
}
