package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer.No
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer.Yes
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType.Audio
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType.Video
import java.util.UUID

class TeleconsultRecordUiRendererTest {

  private val ui = mock<TeleconsultRecordUi>()
  private val uiRenderer = TeleconsultRecordUiRenderer(ui)

  @Test
  fun `when model changes, then render the ui`() {
    // given
    val teleconsultRecordInfo = TestData.teleconsultRecordInfo(
        teleconsultationType = Video,
        patientTookMedicines = No,
        patientConsented = Yes
    )
    val model = TeleconsultRecordModel
        .create(
            patientUuid = UUID.fromString("b1de3894-1efd-44c1-b24e-7eb876517eff"),
            teleconsultRecordId = UUID.fromString("79986364-2fe9-4c74-8607-7b5ff3766bb7")
        )
        .teleconsultRecordLoaded(teleconsultRecordInfo)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).setTeleconsultationType(Video)
    verify(ui).setPatientTookMedicines(No)
    verify(ui).setPatientConsented(Yes)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient details are loaded, then render patient details`() {
    // given
    val patientUuid = UUID.fromString("edd1803b-142e-45a3-950d-238c747cbab7")
    val patient = TestData.patient(
        uuid = patientUuid
    )
    val model = TeleconsultRecordModel
        .create(
            patientUuid = patientUuid,
            teleconsultRecordId = UUID.fromString("9c6e2931-0f33-4560-92a3-8a08865e8103")
        )
        .patientLoaded(patient)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).setTeleconsultationType(Audio)
    verify(ui).setPatientTookMedicines(Yes)
    verify(ui).setPatientConsented(Yes)
    verify(ui).renderPatientDetails(patient)
    verifyNoMoreInteractions(ui)
  }
}
