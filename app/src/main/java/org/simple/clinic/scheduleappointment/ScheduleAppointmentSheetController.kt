package org.simple.clinic.scheduleappointment

import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.overdue.AppointmentConfig
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.scheduler.SchedulersProvider
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

typealias Ui = ScheduleAppointmentUi
typealias UiChange = (Ui) -> Unit

class ScheduleAppointmentSheetController @AssistedInject constructor(
    @Assisted private val patientUuid: UUID,
    @Assisted private val modelSupplier: () -> ScheduleAppointmentModel,
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val config: AppointmentConfig,
    private val clock: UserClock,
    private val facilityRepository: FacilityRepository,
    private val protocolRepository: ProtocolRepository,
    private val currentFacility: Lazy<Facility>,
    private val schedulers: SchedulersProvider
) : ObservableTransformer<UiEvent, UiChange> {

  @AssistedInject.Factory
  interface Factory {
    fun create(
        patientUuid: UUID,
        modelSupplier: () -> ScheduleAppointmentModel
    ): ScheduleAppointmentSheetController
  }

  private val model: ScheduleAppointmentModel
    get() = modelSupplier()

  override fun apply(events: Observable<UiEvent>): Observable<UiChange> {
    return Observable.never()
  }

}
