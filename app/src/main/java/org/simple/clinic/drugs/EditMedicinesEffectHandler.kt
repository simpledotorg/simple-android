package org.simple.clinic.drugs

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.Lazy
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Observables
import org.simple.clinic.drugs.search.DrugFrequency
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import java.util.UUID

class EditMedicinesEffectHandler @AssistedInject constructor(
    private val schedulersProvider: SchedulersProvider,
    private val protocolRepository: ProtocolRepository,
    private val prescriptionRepository: PrescriptionRepository,
    private val facility: Lazy<Facility>,
    private val utcClock: UtcClock,
    private val uuidGenerator: UuidGenerator,
    private val appointmentsRepository: AppointmentRepository,
    private val drugFrequencyToLabelMap: Map<DrugFrequency?, DrugFrequencyLabel>,
    @Assisted private val viewEffectsConsumer: Consumer<EditMedicinesViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(viewEffectsConsumer: Consumer<EditMedicinesViewEffect>): EditMedicinesEffectHandler
  }

  fun build(): ObservableTransformer<EditMedicinesEffect, EditMedicinesEvent> {
    return RxMobius
        .subtypeEffectHandler<EditMedicinesEffect, EditMedicinesEvent>()
        .addTransformer(FetchPrescribedAndProtocolDrugs::class.java, fetchDrugsList(schedulersProvider.io()))
        .addTransformer(RefillMedicines::class.java, refillMedicines())
        .addTransformer(LoadDrugFrequencyChoiceItems::class.java, loadFrequencyChoiceItems())
        .addConsumer(EditMedicinesViewEffect::class.java, viewEffectsConsumer::accept)
        .build()
  }

  private fun loadFrequencyChoiceItems(): ObservableTransformer<LoadDrugFrequencyChoiceItems, EditMedicinesEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .map { drugFrequencyToLabelMap }
          .map(::DrugFrequencyChoiceItemsLoaded)
    }
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

      prescriptionRepository.save(clonedPrescriptions)
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
