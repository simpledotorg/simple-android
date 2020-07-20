package org.simple.clinic.summary.assignedfacility

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class AssignedFacilityUiRendererTest {

  @Test
  fun `when assigned facility is present, then render the assigned facility name`() {
    // given
    val patientUuid = UUID.fromString("b744662a-647a-49c4-aa57-6beb2e194ed1")
    val facility = TestData.facility(
        uuid = UUID.fromString("fe5e67fe-f2c0-4bf8-920b-3aabc669dfae"),
        name = "PHC Obvious"
    )
    val model = AssignedFacilityModel.create(patientUuid)
        .assignedFacilityUpdated(facility)

    val ui = mock<AssignedFacilityUi>()
    val uiRenderer = AssignedFacilityUiRenderer(ui)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderAssignedFacilityName("PHC Obvious")
    verifyNoMoreInteractions(ui)
  }
}
