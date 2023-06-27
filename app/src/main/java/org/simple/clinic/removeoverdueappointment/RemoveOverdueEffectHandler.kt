package org.simple.clinic.removeoverdueappointment

import com.spotify.mobius.functions.Consumer
import com.spotify.mobius.rx2.RxMobius
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.ObservableTransformer
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.SchedulersProvider

class RemoveOverdueEffectHandler @AssistedInject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val cancelAppointmentWithReason: CancelAppointmentWithReason,
    private val schedulersProvider: SchedulersProvider,
    @Assisted private val viewEffectsConsumer: Consumer<RemoveOverdueViewEffect>
) {

  @AssistedFactory
  interface Factory {
    fun create(viewEffectsConsumer: Consumer<RemoveOverdueViewEffect>): RemoveOverdueEffectHandler
  }

  fun build(): ObservableTransformer<RemoveOverdueEffect, RemoveOverdueEvent> = RxMobius
      .subtypeEffectHandler<RemoveOverdueEffect, RemoveOverdueEvent>()
      .addTransformer(MarkPatientAsVisited::class.java, markPatientAsVisited())
      .addTransformer(MarkPatientAsDead::class.java, markPatientAsDead())
      .addTransformer(CancelAppointment::class.java, cancelAppointment())
      .addTransformer(MarkPatientAsMovedToPrivate::class.java, markPatientAsMovedToPrivate())
      .addTransformer(MarkPatientAsTransferredToAnotherFacility::class.java, markPatientAsMovedToAnotherFacility())
      .addTransformer(MarkPatientAsRefusedToComeBack::class.java, markPatientAsRefusedToComeBack())
      .addConsumer(RemoveOverdueViewEffect::class.java, viewEffectsConsumer::accept)
      .build()

  private fun markPatientAsMovedToAnotherFacility(): ObservableTransformer<MarkPatientAsTransferredToAnotherFacility, RemoveOverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { patientRepository.updatePatientStatusToMigrated(it.patientId) }
          .map { AppointmentCancelReason.TransferredToAnotherPublicHospital }
          .map(::PatientMarkedAsMigrated)
    }
  }

  private fun markPatientAsMovedToPrivate(): ObservableTransformer<MarkPatientAsMovedToPrivate, RemoveOverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { patientRepository.updatePatientStatusToMigrated(it.patientId) }
          .map { AppointmentCancelReason.MovedToPrivatePractitioner }
          .map(::PatientMarkedAsMigrated)
    }
  }

  private fun markPatientAsRefusedToComeBack(): ObservableTransformer<MarkPatientAsRefusedToComeBack, RemoveOverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { patientRepository.updatePatientStatusToMigrated(it.patientId) }
          .map { AppointmentCancelReason.RefusedToComeBack }
          .map(::PatientMarkedAsMigrated)
    }
  }

  private fun cancelAppointment(): ObservableTransformer<CancelAppointment, RemoveOverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { cancelAppointmentWithReason.execute(it.appointment, it.reason) }
          .map { AppointmentMarkedAsCancelled }
    }
  }

  private fun markPatientAsDead(): ObservableTransformer<MarkPatientAsDead, RemoveOverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { patientRepository.updatePatientStatusToDead(it.patientId) }
          .map { PatientMarkedAsDead }
    }
  }

  private fun markPatientAsVisited(): ObservableTransformer<MarkPatientAsVisited, RemoveOverdueEvent> {
    return ObservableTransformer { effects ->
      effects
          .observeOn(schedulersProvider.io())
          .doOnNext { appointmentRepository.markAsAlreadyVisited(it.appointmentUuid) }
          .map { PatientMarkedAsVisited }
    }
  }
}
