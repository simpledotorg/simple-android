package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class TeleconsultRecordUpdateTest {

  private val updateSpec = UpdateSpec(TeleconsultRecordUpdate())
  private val defaultModel = TeleconsultRecordModel.create()

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
            id = UUID.fromString("4dfd6385-5628-4956-aeb9-0b6dda06ea10"),
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
}
