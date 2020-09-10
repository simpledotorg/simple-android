package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer.Yes
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType.Audio
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.time.Instant
import java.util.UUID

class TeleconsultRecordEffectHandlerTest {

  private val teleconsultRecordId = UUID.fromString("7e9111e5-7707-434a-af36-dad313f406ee")
  private val user = TestData.loggedInUser(
      uuid = UUID.fromString("b5a3aabe-ec33-4e0b-a7a9-b2e4bd4c4303")
  )
  private val utcClock = TestUtcClock(instant = Instant.parse("2018-01-01T00:00:00Z"))
  private val teleconsultRecordRepository = mock<TeleconsultRecordRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val uiActions = mock<UiActions>()

  private val effectHandler = TeleconsultRecordEffectHandler(
      user = { user },
      teleconsultRecordRepository = teleconsultRecordRepository,
      patientRepository = patientRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      utcClock = utcClock,
      uiActions = uiActions
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when go back effect is received, then go back to previous screen`() {
    // when
    effectHandlerTestCase.dispatch(GoBack)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).goBackToPreviousScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when navigate to teleconsult success screen effect is received, then navigate to teleconsult success screen`() {
    // when
    effectHandlerTestCase.dispatch(NavigateToTeleconsultSuccess)

    // then
    effectHandlerTestCase.assertNoOutgoingEvents()

    verify(uiActions).navigateToTeleconsultSuccessScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when load teleconsult record with prescribed drugs effect is received, then load the teleconsult record with prescribed drugs`() {
    // given
    val teleconsultRecordWithPrescribedDrugs = TestData.teleconsultRecordWithPrescribedDrugs(
        teleconsultRecord = TestData.teleconsultRecord(
            id = teleconsultRecordId
        ),
        prescribedDrugs = emptyList()
    )

    whenever(teleconsultRecordRepository.getTeleconsultRecordWithPrescribedDrugs(teleconsultRecordId)) doReturn teleconsultRecordWithPrescribedDrugs

    // when
    effectHandlerTestCase.dispatch(LoadTeleconsultRecordWithPrescribedDrugs(teleconsultRecordId))

    // then
    effectHandlerTestCase.assertOutgoingEvents(TeleconsultRecordWithPrescribedDrugsLoaded(teleconsultRecordWithPrescribedDrugs))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when create teleconsult record effect is received, then create the teleconsult record`() {
    // given
    val patientUuid = UUID.fromString("cd8c77c4-67de-482a-bfd0-2b035291b45d")
    val teleconsultRecord = TestData.teleconsultRecord(
        id = teleconsultRecordId,
        patientId = patientUuid,
        medicalOfficerId = user.uuid,
        teleconsultRecordInfo = TestData.teleconsultRecordInfo(
            recordedAt = Instant.parse("2018-01-01T00:00:00Z"),
            teleconsultationType = Audio,
            patientTookMedicines = Yes,
            patientConsented = Yes,
            medicalOfficerNumber = null
        ),
        teleconsultRequestInfo = null,
        createdAt = Instant.now(utcClock),
        updatedAt = Instant.now(utcClock)
    )

    // when
    effectHandlerTestCase.dispatch(CreateTeleconsultRecord(
        teleconsultRecordId = teleconsultRecordId,
        patientUuid = patientUuid,
        teleconsultationType = Audio,
        patientTookMedicine = Yes,
        patientConsented = Yes
    ))

    // then
    effectHandlerTestCase.assertOutgoingEvents(TeleconsultRecordCreated)

    verify(teleconsultRecordRepository).createTeleconsultRecordForMedicalOfficer(
        teleconsultRecordId = teleconsultRecord.id,
        patientUuid = teleconsultRecord.patientId,
        medicalOfficerId = teleconsultRecord.medicalOfficerId,
        teleconsultRecordInfo = teleconsultRecord.teleconsultRecordInfo!!
    )
    verifyNoMoreInteractions(teleconsultRecordRepository)

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when load patient details effect is received, then load the patient details`() {
    // given
    val patientUuid = UUID.fromString("faacb4f7-50c9-480d-b154-6314a5e67d63")
    val patient = TestData.patient(
        uuid = patientUuid,
        fullName = "Peter Parker"
    )

    whenever(patientRepository.patientImmediate(patientUuid)) doReturn patient

    // when
    effectHandlerTestCase.dispatch(LoadPatientDetails(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(PatientDetailsLoaded(patient))

    verifyZeroInteractions(uiActions)
  }
}
