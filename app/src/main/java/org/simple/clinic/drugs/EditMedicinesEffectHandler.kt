package org.simple.clinic.drugs

import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Observables
import org.simple.clinic.drugs.selection.EditMedicinesUiActions
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import java.util.UUID

class EditMedicinesEffectHandler @AssistedInject constructor(
    @Assisted private val uiActions: EditMedicinesUiActions,
    private val schedulersProvider: SchedulersProvider,
    private val protocolRepository: ProtocolRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val facility: Lazy<Facility>,
    private val utcClock: UtcClock,
    private val uuidGenerator: UuidGenerator,
    private val appointmentsRepository: AppointmentRepository
) {

  @AssistedFactory
  interface Factory {
    fun create(uiActions: EditMedicinesUiActions): EditMedicinesEffectHandler
  }

  fun build(): ObservableTransformer<EditMedicinesEffect, EditMedicinesEvent> {
    return RxMobius
        .subtypeEffectHandler<EditMedicinesEffect, EditMedicinesEvent>()
        .addTransformer(FetchPrescribedAndProtocolDrugs::class.java, fetchDrugsList(schedulersProvider.io()))
        .addConsumer(ShowNewPrescriptionEntrySheet::class.java, { uiActions.showNewPrescriptionEntrySheet(it.patientUuid) }, schedulersProvider.ui())
        .addConsumer(OpenDosagePickerSheet::class.java, { uiActions.showDosageSelectionSheet(it.drugName, it.patientUuid, it.prescribedDrugUuid) }, schedulersProvider.ui())
        .addConsumer(ShowUpdateCustomPrescriptionSheet::class.java, { uiActions.showUpdateCustomPrescriptionSheet(it.prescribedDrug) }, schedulersProvider.ui())
        .addAction(GoBackToPatientSummary::class.java, uiActions::goBackToPatientSummary, schedulersProvider.ui())
        .addTransformer(RefillMedicines::class.java, refillMedicines())
        .build()
  }

  private fun refillMedicines(): ObservableTransformer<RefillMedicines, EditMedicinesEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { effect -> prescriptionRepository.newestPrescriptionsForPatientImmediate(effect.patientUuid) to effect }
          .doOnNext { (prescriptions, _) -> clonePrescriptions(prescriptions) }
          .doOnNext { (_, effect) -> updateAppointmentsAsVisited(effect.patientUuid) }
          .map { PrescribedMedicinesRefilled }
    }
  }

  private fun clonePrescriptions(
      prescriptions: List<PrescribedDrug>,
  ) {
    if (prescriptions.isNotEmpty()) {
      prescriptionRepository.softDeletePrescriptions(prescriptions)

      val clonedPrescriptions = prescriptions.map { prescribedDrug ->
        prescribedDrug.refill(
            uuid = uuidGenerator.v4(),
            facilityUuid = facility.get().uuid,
            utcClock = utcClock
        )
      }

      prescriptionRepository.saveImmediate(clonedPrescriptions)
    }
  }

  private fun updateAppointmentsAsVisited(patientUuid: PatientUuid) {
    appointmentsRepository.markAppointmentsCreatedBeforeTodayAsVisited(patientUuid)
  }

  private fun fetchDrugsList(io: Scheduler): ObservableTransformer<FetchPrescribedAndProtocolDrugs, EditMedicinesEvent> {
    return ObservableTransformer { effects ->

      val protocolDrugsStream = effects
          .observeOn(io)
          .map { currentProtocolUuid() }
          .map { protocolRepository.drugsForProtocolOrDefault(it) }

      val prescribedDrugsStream = effects
          .switchMap { prescriptionRepository.newestPrescriptionsForPatient(it.patientUuid).subscribeOn(io) }

      Observables
          .combineLatest(protocolDrugsStream, prescribedDrugsStream)
          .map { DrugsListFetched(it.first, it.second) }
    }
  }

  private fun currentProtocolUuid(): UUID {
    return facility.get().protocolUuid!!
  }
}
