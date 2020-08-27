package org.simple.clinic.summary.teleconsultation.contactdoctor

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class ContactDoctorUiRendererTest {

  @Test
  fun `when medical officers are loaded, then show medical officers`() {
    // given
    val medicalOfficers = listOf(
        TestData.medicalOfficer(
            id = UUID.fromString("2083b744-3b59-478f-b222-12466d5badb7"),
            fullName = "Dr Ramesh",
            phoneNumber = "+911111111111"
        )
    )
    val model = ContactDoctorModel.create()
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
