package org.simple.clinic.drugs

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Completable
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.drugs.selection.EditMedicinesUiActions
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.storage.Timestamps
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import java.time.Duration
import java.time.Instant
import java.util.UUID

class EditMedicineEffectHandlerTest {

  private val uiActions = mock<EditMedicinesUiActions>()
  private val protocolRepository = mock<ProtocolRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val facility = TestData.facility(uuid = UUID.fromString("94db5d90-d483-4755-892a-97fde5a870fe"))
  private val utcClock = TestUtcClock(instant = Instant.parse("2018-01-01T00:00:00Z"))
  private val uuidGenerator = mock<UuidGenerator>()
  private val appointmentRepository = mock<AppointmentRepository>()

  private val effectHandler = EditMedicinesEffectHandler(
      uiActions = uiActions,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      protocolRepository = protocolRepository,
      prescriptionRepository = prescriptionRepository,
      facility = Lazy { facility },
      utcClock = utcClock,
      uuidGenerator = uuidGenerator,
      appointmentsRepository = appointmentRepository
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when refill medicine effect is received, then clone the prescription and save it in the repository`() {
    // given
    val durationToAdvanceBy = Duration.ofMinutes(20)
    val patientUuid = UUID.fromString("7bb4616b-1542-46ad-93d5-9d03b619aa99")
    val facilityUuid = UUID.fromString("32dc8f5c-47b1-453a-b1d9-aa9230630c86")

    val clonedDrug1Uuid = UUID.fromString("6d13e4f4-c0ee-4dc8-985b-e7cd82c69ffa")
    val clonedDrug2Uuid = UUID.fromString("348f864b-cec6-4823-9dc0-e9dd4dfe5379")

    val drug1 = TestData.prescription(
        uuid = UUID.fromString("95ec779b-f862-4799-92c9-f9d1899af59a"),
        name = "Drug 1",
        dosage = "10 mg",
        rxNormCode = "rx-norm-code",
        isDeleted = false,
        isProtocolDrug = false,
        patientUuid = patientUuid,
        facilityUuid = facilityUuid,
        syncStatus = SyncStatus.DONE,
        timestamps = Timestamps.create(utcClock),
        frequency = MedicineFrequency.TDS,
        durationInDays = 30,
        teleconsultationId = UUID.fromString("d0e47d72-6773-4333-8447-bbd1c5004088")
    )
    val drug2 = TestData.prescription(
        uuid = UUID.fromString("b0a39aba-42a1-447b-922a-6223dacf6868"),
        name = "Drug 2",
        dosage = "20 mg",
        rxNormCode = "rx-norm-code",
        isDeleted = false,
        isProtocolDrug = false,
        patientUuid = patientUuid,
        facilityUuid = facilityUuid,
        syncStatus = SyncStatus.DONE,
        timestamps = Timestamps.create(utcClock),
        frequency = MedicineFrequency.TDS,
        durationInDays = 30,
        teleconsultationId = UUID.fromString("d0e47d72-6773-4333-8447-bbd1c5004088")
    )
    val prescriptions = listOf(drug1, drug2)

    val clonedPrescriptions = listOf(
        drug1.copy(
            uuid = clonedDrug1Uuid,
            facilityUuid = facility.uuid,
            syncStatus = SyncStatus.PENDING,
            timestamps = drug1.timestamps.copy(
                createdAt = drug1.createdAt.plus(durationToAdvanceBy),
                updatedAt = drug1.updatedAt.plus(durationToAdvanceBy),
                deletedAt = null
            ),
            frequency = null,
            durationInDays = null,
            teleconsultationId = null
        ),
        drug2.copy(
            uuid = clonedDrug2Uuid,
            facilityUuid = facility.uuid,
            syncStatus = SyncStatus.PENDING,
            timestamps = drug2.timestamps.copy(
                createdAt = drug2.createdAt.plus(durationToAdvanceBy),
                updatedAt = drug2.updatedAt.plus(durationToAdvanceBy),
                deletedAt = null
            ),
            frequency = null,
            durationInDays = null,
            teleconsultationId = null
        )
    )

    whenever(uuidGenerator.v4()).thenReturn(clonedDrug1Uuid, clonedDrug2Uuid)
    whenever(prescriptionRepository.newestPrescriptionsForPatientImmediate(patientUuid)) doReturn prescriptions
    whenever(prescriptionRepository.save(clonedPrescriptions)) doReturn Completable.complete()

    // when
    utcClock.advanceBy(durationToAdvanceBy)
    testCase.dispatch(RefillMedicines(patientUuid))

    // then
    testCase.assertOutgoingEvents(PrescribedMedicinesRefilled)

    verify(prescriptionRepository).newestPrescriptionsForPatientImmediate(patientUuid)
    verify(prescriptionRepository).softDeletePrescriptions(prescriptions)
    verify(prescriptionRepository).saveImmediate(clonedPrescriptions)
    verify(appointmentRepository).markAppointmentsCreatedBeforeTodayAsVisited(patientUuid)
    verifyNoMoreInteractions(prescriptionRepository)
    verifyNoMoreInteractions(appointmentRepository)

    verifyZeroInteractions(uiActions)
  }
}
