package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.sharedTestCode.TestData
import java.util.UUID

class TeleconsultDoctorInfoUiRendererTest {

  @Test
  fun `when user is loaded, then render user acknowledgement`() {
    // given
    val ui = mock<TeleconsultDoctorInfoUi>()
    val uiRenderer = TeleconsultDoctorInfoUiRenderer(ui)

    val user = TestData.loggedInUser(uuid = UUID.fromString("ab12ad50-2786-4e94-a8d3-003e0a6d5e07"))
    val model = TeleconsultDoctorInfoModel.create()
        .currentUserLoaded(user)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).renderDoctorAcknowledgement(user)
    verifyNoMoreInteractions(ui)
  }
}
