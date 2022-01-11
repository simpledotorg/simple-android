package org.simple.clinic.summary.assignedfacility

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.Optional
import java.util.UUID

class AssignedFacilityEffectHandlerTest {

  private val patientRepository = mock<PatientRepository>()
  private val facilityRepository = mock<FacilityRepository>()
  private val uiActions = mock<UiActions>()

  private val effectHandler = AssignedFacilityEffectHandler(
      patientRepository = patientRepository,
      facilityRepository = facilityRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions
  ).build()
  private val effectHandlerTestCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    effectHandlerTestCase.dispose()
  }

  @Test
  fun `when load assigned facility effect is received, then load assigned facility`() {
    // given
    val assignedFacilityUuid = UUID.fromString("0ef45f88-21a1-4620-ae4d-94395bbbbb87")
    val patientUuid = UUID.fromString("17bb9690-9a17-4d90-a45f-8bfdcc4153e4")
    val patient = TestData.patient(
        uuid = patientUuid,
        registeredFacilityId = UUID.fromString("7e101cd0-572c-401d-a1a5-ab5f9a925ad2"),
        assignedFacilityId = assignedFacilityUuid
    )
    val facility = TestData.facility(
        uuid = assignedFacilityUuid,
        name = "CHC Obvious"
    )

    whenever(patientRepository.patientImmediate(patientUuid)) doReturn patient
    whenever(facilityRepository.facility(assignedFacilityUuid)) doReturn Optional.of(facility)

    // when
    effectHandlerTestCase.dispatch(LoadAssignedFacility(patientUuid))

    // then
    effectHandlerTestCase.assertOutgoingEvents(AssignedFacilityLoaded(Optional.of(facility)))

    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when change assigned facility effect is received, then change the assigned facility`() {
    // given
    val patientUuid = UUID.fromString("163d66b6-b7a4-47f3-8aea-257636489956")
    val updatedAssignedFacilityId = UUID.fromString("bf9ff0b1-c859-4ff9-9dfe-544e5fbc11d3")

    // when
    effectHandlerTestCase.dispatch(ChangeAssignedFacility(patientUuid, updatedAssignedFacilityId))

    // then
    verify(patientRepository).updateAssignedFacilityId(
        patientId = patientUuid,
        assignedFacilityId = updatedAssignedFacilityId
    )

    effectHandlerTestCase.assertOutgoingEvents(AssignedFacilityChanged)
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when notify assigned facility effect is changed, then notify the assigned facility changed`() {
    // when
    effectHandlerTestCase.dispatch(NotifyAssignedFacilityChanged)

    // then
    verify(uiActions).notifyAssignedFacilityChanged()
    verifyNoMoreInteractions(uiActions)
  }
}
