package org.simple.clinic.summary.teleconsultation.contactdoctor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityRepository
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordRepository
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class ContactDoctorEffectHandlerTest {

  private val teleconsultRecordId = UUID.fromString("06301354-6492-4e57-bcd7-09f7a9eb7860")
  private val uuidGenerator = FakeUuidGenerator.fixed(teleconsultRecordId)
  private val teleconsultationFacilityRepository = mock<TeleconsultationFacilityRepository>()
  private val teleconsultRecordRepository = mock<TeleconsultRecordRepository>()
  private val user = TestData.loggedInUser(
      uuid = UUID.fromString("bd0d2249-5a40-4782-b88a-4e05c24a50a5")
  )
  private val facility = TestData.facility(
      uuid = UUID.fromString("2b7a809c-70de-40d3-a77f-d6bffcba3cfe")
  )
  private val clock = TestUtcClock(LocalDate.parse("2018-01-01"))
  private val effectHandler = ContactDoctorEffectHandler(
      currentUser = { user },
      currentFacility = { facility },
      teleconsultationFacilityRepository = teleconsultationFacilityRepository,
      teleconsultRecordRepository = teleconsultRecordRepository,
      uuidGenerator = uuidGenerator,
      clock = clock,
      schedulersProvider = TestSchedulersProvider.trampoline()
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load medical officers effect is received, then load medical officers for the current facility`() {
    // given
    val medicalOfficers = listOf(
        TestData.medicalOfficer(
            id = UUID.fromString("6240e551-9b50-46ca-9de7-23d9bc66afe1"),
            fullName = "Dr Sunil Gupta",
            phoneNumber = "+911111111111"
        ),
        TestData.medicalOfficer(
            id = UUID.fromString("44fb8a5e-e737-46a5-9d5f-bffa4e513ff7"),
            fullName = "Dr Mahesh Bhatt",
            phoneNumber = "+912222222222"
        )
    )
    whenever(teleconsultationFacilityRepository.medicalOfficersForFacility(facility.uuid)) doReturn medicalOfficers

    // when
    effectHandlerTestCase.dispatch(LoadMedicalOfficers)

    // then
    effectHandlerTestCase.assertOutgoingEvents(MedicalOfficersLoaded(medicalOfficers))
  }

  @Test
  fun `when create teleconsult request for nurse effect is received, then create the teleconsult request`() {
    // given
    val patientUuid = UUID.fromString("ca25eef5-687c-408a-83c9-83c68a3f3986")
    val medicalOfficerId = UUID.fromString("f403696a-db38-4058-9b51-37f0db07207b")
    val doctorPhoneNumber = "+911111111111"

    val teleconsultRequestInfo = TestData.teleconsultRequestInfo(
        requesterId = user.uuid,
        facilityId = facility.uuid,
        requestedAt = Instant.now(clock)
    )

    // when
    effectHandlerTestCase.dispatch(CreateTeleconsultRequest(patientUuid, medicalOfficerId, doctorPhoneNumber))

    // then
    effectHandlerTestCase.assertOutgoingEvents(TeleconsultRequestCreated(teleconsultRecordId, doctorPhoneNumber))

    verify(teleconsultRecordRepository).createTeleconsultRequestForNurse(
        teleconsultRecordId = teleconsultRecordId,
        patientUuid = patientUuid,
        medicalOfficerId = medicalOfficerId,
        teleconsultRequestInfo = teleconsultRequestInfo
    )
    verifyNoMoreInteractions(teleconsultRecordRepository)
  }
}
