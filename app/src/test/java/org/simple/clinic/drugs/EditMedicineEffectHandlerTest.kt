package org.simple.clinic.drugs

import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import dagger.Lazy
import org.junit.After
import org.junit.Test
import org.simple.clinic.drugs.search.DrugFrequency.BD
import org.simple.clinic.drugs.search.DrugFrequency.OD
import org.simple.clinic.drugs.search.DrugFrequency.QDS
import org.simple.clinic.drugs.search.DrugFrequency.TDS
import org.simple.clinic.drugs.selection.EditMedicinesUiActions
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyLabel
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.overdue.AppointmentRepository
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.protocol.ProtocolRepository
import org.simple.clinic.teleconsultlog.medicinefrequency.MedicineFrequency
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.UuidGenerator
import org.simple.sharedTestCode.TestData
import java.util.UUID

class EditMedicineEffectHandlerTest {

  private val uiActions = mock<EditMedicinesUiActions>()
  private val protocolRepository = mock<ProtocolRepository>()
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val facility = TestData.facility(uuid = UUID.fromString("94db5d90-d483-4755-892a-97fde5a870fe"))
  private val uuidGenerator = mock<UuidGenerator>()
  private val appointmentRepository = mock<AppointmentRepository>()

  private val drugFrequencyToLabelMap = mapOf(
      null to DrugFrequencyLabel(label = "None"),
      OD to DrugFrequencyLabel(label = "OD"),
      BD to DrugFrequencyLabel(label = "BD"),
      TDS to DrugFrequencyLabel(label = "TDS"),
      QDS to DrugFrequencyLabel(label = "QDS")
  )

  val viewEffectsConsumer = EditMedicinesViewEffectHandler(uiActions)::handle

  private val effectHandler = EditMedicinesEffectHandler(
      schedulersProvider = TestSchedulersProvider.trampoline(),
      protocolRepository = protocolRepository,
      prescriptionRepository = prescriptionRepository,
      facility = Lazy { facility },
      uuidGenerator = uuidGenerator,
      appointmentsRepository = appointmentRepository,
      drugFrequencyToLabelMap = drugFrequencyToLabelMap,
      viewEffectsConsumer = viewEffectsConsumer
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when refill medicine effect is received, then clone the prescription and save it in the repository`() {
    // given
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
        frequency = MedicineFrequency.TDS,
        durationInDays = 30,
        teleconsultationId = UUID.fromString("d0e47d72-6773-4333-8447-bbd1c5004088")
    )
    val prescriptions = listOf(drug1, drug2)

    whenever(uuidGenerator.v4()).thenReturn(clonedDrug1Uuid, clonedDrug2Uuid)
    whenever(prescriptionRepository.newestPrescriptionsForPatientImmediate(patientUuid)) doReturn prescriptions

    // when
    testCase.dispatch(RefillMedicines(patientUuid))

    // then
    testCase.assertOutgoingEvents(PrescribedMedicinesRefilled)

    verify(prescriptionRepository).newestPrescriptionsForPatientImmediate(patientUuid)
    verify(prescriptionRepository).refill(
        prescriptions = eq(prescriptions),
        uuidGenerator = any()
    )
    verify(appointmentRepository).markAppointmentsCreatedBeforeTodayAsVisited(patientUuid)
    verifyNoMoreInteractions(prescriptionRepository)
    verifyNoMoreInteractions(appointmentRepository)

    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when load drug frequency choice items effect is received, then load drug frequency choice items`() {
    // when
    testCase.dispatch(LoadDrugFrequencyChoiceItems)

    // then
    testCase.assertOutgoingEvents(DrugFrequencyChoiceItemsLoaded(drugFrequencyToLabelMap))
    verifyNoInteractions(uiActions)
  }
}
