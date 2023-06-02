package org.simple.clinic.bloodsugar.history

import org.mockito.kotlin.mock
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.bloodsugar.BloodSugarHistoryListItemDataSourceFactory
import org.simple.sharedTestCode.TestData
import java.util.UUID

class BloodSugarHistoryScreenUpdateTest {
  private val patientUuid = UUID.fromString("871a2f40-2bda-488c-9443-7dc708c3743a")
  private val defaultModel = BloodSugarHistoryScreenModel.create(patientUuid)
  private val updateSpec = UpdateSpec<BloodSugarHistoryScreenModel, BloodSugarHistoryScreenEvent, BloodSugarHistoryScreenEffect>(BloodSugarHistoryScreenUpdate())

  @Test
  fun `when patient is loaded, then show patient information`() {
    val patient = TestData.patient(uuid = patientUuid)

    updateSpec
        .given(defaultModel)
        .whenEvent(PatientLoaded(patient))
        .then(assertThatNext(
            hasModel(defaultModel.patientLoaded(patient)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when add new blood sugar is clicked, then open entry sheet`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(AddNewBloodSugarClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenBloodSugarEntrySheet(patientUuid) as BloodSugarHistoryScreenEffect)
        ))
  }

  @Test
  fun `when blood sugar is clicked, then open update sheet`() {
    val bloodSugar = TestData.bloodSugarMeasurement()

    updateSpec
        .given(defaultModel)
        .whenEvent(BloodSugarClicked(bloodSugar))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenBloodSugarUpdateSheet(bloodSugar) as BloodSugarHistoryScreenEffect)
        ))
  }

  @Test
  fun `when blood sugar history is loaded, then show blood sugar`() {
    val bloodSugarsHistoryListItemDataSourceFactory = mock<BloodSugarHistoryListItemDataSourceFactory>()

    updateSpec
        .given(defaultModel)
        .whenEvent(BloodSugarHistoryLoaded(bloodSugarsHistoryListItemDataSourceFactory))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowBloodSugars(bloodSugarsHistoryListItemDataSourceFactory))
        ))
  }
}
