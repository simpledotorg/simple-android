package org.simple.clinic.summary.linkId

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

class LinkIdWithPatientUiRendererTest {

  private val ui = mock<LinkIdWithPatientViewUi>()

  private val patientUuid = UUID.fromString("9e2bd2b6-d50c-4dfb-bd4a-c4119ac80365")
  private val patientName = "TestName"
  private val identifier = Identifier(
      value = "40269f4d-f177-44a5-9db7-3cb8a7a53b33",
      type = Identifier.IdentifierType.BpPassport
  )

  private val patientNameFetchedModel = LinkIdWithPatientModel
      .create()
      .linkIdWithPatientViewShown(patientUuid, identifier)
      .patientNameFetched(patientName)

  private val linkIdWithPatientUiRenderer = LinkIdWithPatientUiRenderer(ui)

  @Test
  fun `when patient name is fetched, then show patient name`() {
    // when
    linkIdWithPatientUiRenderer.render(patientNameFetchedModel)

    // then
    verify(ui).renderPatientName(patientName)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when identifier is being to patient, then show button progress`() {
    // given
    val buttonSavingStateModel = patientNameFetchedModel
        .saving()

    // when
    linkIdWithPatientUiRenderer.render(buttonSavingStateModel)

    // then
    verify(ui).renderPatientName(patientName)
    verify(ui).showAddButtonProgress()
    verifyNoMoreInteractions(ui)
  }
}
