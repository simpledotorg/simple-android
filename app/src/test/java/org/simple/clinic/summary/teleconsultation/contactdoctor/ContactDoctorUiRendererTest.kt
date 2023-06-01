package org.simple.clinic.summary.teleconsultation.contactdoctor

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test
import org.simple.sharedTestCode.TestData
import java.util.UUID

class ContactDoctorUiRendererTest {

  @Test
  fun `when medical officers are loaded, then show medical officers`() {
    // given
    val patientUuid = UUID.fromString("477e622c-91f4-428f-86ea-557876ef3be7")
    val medicalOfficers = listOf(
        TestData.medicalOfficer(
            id = UUID.fromString("2083b744-3b59-478f-b222-12466d5badb7"),
            fullName = "Dr Ramesh",
            phoneNumber = "+911111111111"
        )
    )
    val model = ContactDoctorModel.create(patientUuid)
        .medicalOfficersLoaded(medicalOfficers)

    val ui = mock<ContactDoctorUi>()
    val uiRenderer = ContactDoctorUiRenderer(ui)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showMedicalOfficers(medicalOfficers)
    verifyNoMoreInteractions(ui)
  }
}
