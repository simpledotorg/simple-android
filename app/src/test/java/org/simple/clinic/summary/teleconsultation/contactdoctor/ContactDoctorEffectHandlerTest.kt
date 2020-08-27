package org.simple.clinic.summary.teleconsultation.contactdoctor

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.summary.teleconsultation.sync.TeleconsultationFacilityRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class ContactDoctorEffectHandlerTest {

  private val teleconsultationFacilityRepository = mock<TeleconsultationFacilityRepository>()
  private val facility = TestData.facility(
      uuid = UUID.fromString("2b7a809c-70de-40d3-a77f-d6bffcba3cfe")
  )
  private val effectHandler = ContactDoctorEffectHandler(
      currentFacility = { facility },
      teleconsultationFacilityRepository = teleconsultationFacilityRepository,
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
}
