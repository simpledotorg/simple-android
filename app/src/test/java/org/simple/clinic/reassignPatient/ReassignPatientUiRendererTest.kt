package org.simple.clinic.reassignPatient

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.simple.sharedTestCode.TestData
import java.util.UUID

class ReassignPatientUiRendererTest {
  @Test
  fun `when assigned facility is present, then render the assigned facility name`() {
    // given
    val facility = TestData.facility(
        uuid = UUID.fromString("fefd8415-3d16-4c86-99c9-edab413a5e95"),
        name = "PHC Doha"
    )
    val model = ReassignPatientModel.create()
        .assignedFacilityUpdated(facility)

    val ui = mock<ReassignPatientUi>()
    val uiRenderer = ReassignPatientUiRenderer(ui)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderAssignedFacilityName(facility.name)
    verifyNoMoreInteractions(ui)
  }
}
