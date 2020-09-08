package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer.Yes
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType.Audio
import java.util.UUID

class TeleconsultRecordUpdateTest {

  private val patientUuid = UUID.fromString("297fa0ea-5c20-4772-9bf8-7f42749dfedc")
  private val teleconsultRecordId = UUID.fromString("39c41027-6c4d-4c53-a9b4-b1371746f859")
  private val updateSpec = UpdateSpec(TeleconsultRecordUpdate())
  private val defaultModel = TeleconsultRecordModel.create(patientUuid, teleconsultRecordId)

  @Test
  fun `when back is clicked, then navigate to previous screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(BackClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoBack)
        ))
  }

  @Test
  fun `when teleconsult record is created, then navigate to teleconsult success screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(TeleconsultRecordCreated)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(NavigateToTeleconsultSuccess)
        ))
  }

  @Test
  fun `update model, when teleconsult record already exists`() {
    val teleconsultRecordWithPrescribedDrugs = TestData.teleconsultRecordWithPrescribedDrugs(
        teleconsultRecord = TestData.teleconsultRecord(
            id = teleconsultRecordId,
            teleconsultRecordInfo = TestData.teleconsultRecordInfo()
        ),
        prescribedDrugs = emptyList()
    )
    val teleconsultRecordInfo = teleconsultRecordWithPrescribedDrugs.teleconsultRecord.teleconsultRecordInfo!!

    updateSpec
        .given(defaultModel)
        .whenEvent(TeleconsultRecordWithPrescribedDrugsLoaded(teleconsultRecordWithPrescribedDrugs))
        .then(assertThatNext(
            hasModel(defaultModel.teleconsultRecordLoaded(teleconsultRecordInfo))
        ))
  }

  @Test
  fun `when done is clicked, then create the teleconsult record`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DoneClicked(
            teleconsultationType = Audio,
            patientTookMedicines = Yes,
            patientConsented = Yes
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CreateTeleconsultRecord(
                patientUuid = patientUuid,
                teleconsultRecordId = teleconsultRecordId,
                teleconsultationType = Audio,
                patientTookMedicine = Yes,
                patientConsented = Yes
            ))
        ))
  }

  @Test
  fun `when patient details are loaded, then update the model`() {
    val patient = TestData.patient(
        uuid = patientUuid
    )
    updateSpec
        .given(defaultModel)
        .whenEvent(PatientDetailsLoaded(patient))
        .then(assertThatNext(
            hasModel(defaultModel.patientLoaded(patient)),
            hasNoEffects()
        ))
  }
}
