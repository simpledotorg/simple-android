package org.simple.clinic.editpatient.deletepatient

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Test
import java.util.UUID

class DeletePatientViewRendererTest {

  private val patientDeleteReasons = listOf(
      PatientDeleteReason.Duplicate,
      PatientDeleteReason.AccidentalRegistration,
      PatientDeleteReason.Died
  )
  private val ui = mock<DeletePatientUi>()
  private val viewRenderer = DeletePatientViewRenderer(ui)

  private val patientUuid = UUID.fromString("7376ca9a-ae07-45e4-b667-7b4dcc460b45")
  private val defaultModel = DeletePatientModel.default(patientUuid)

  @Test
  fun `when patient name is loaded, then show the delete reasons`() {
    // given
    val model = defaultModel.patientNameLoaded("John Doe")

    // when
    viewRenderer.render(model)

    // then
    verify(ui).showDeleteReasons(patientDeleteReasons = patientDeleteReasons, selectedReason = model.selectedReason)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient name is not loaded, then do nothing`() {
    // when
    viewRenderer.render(defaultModel)

    // then
    verifyZeroInteractions(ui)
  }
}
