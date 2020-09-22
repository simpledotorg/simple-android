package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
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
