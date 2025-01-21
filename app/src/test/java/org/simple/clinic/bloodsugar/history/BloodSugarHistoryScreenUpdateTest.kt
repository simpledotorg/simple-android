package org.simple.clinic.bloodsugar.history

import androidx.paging.PagingData
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.bloodsugar.history.adapter.BloodSugarHistoryListItem
import org.simple.clinic.TestData
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class BloodSugarHistoryScreenUpdateTest {
  private val patientUuid = UUID.fromString("871a2f40-2bda-488c-9443-7dc708c3743a")
  private val defaultModel = BloodSugarHistoryScreenModel.create(patientUuid)
  private val updateSpec = UpdateSpec(BloodSugarHistoryScreenUpdate())

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
  fun `when blood sugar history is loaded, then update state`() {
    val patientUuid = UUID.fromString("12515571-10ac-411c-9f65-7a5a91e02538")
    val createdAt = Instant.parse("2020-01-01T00:00:00Z")
    val recordedAt = Instant.parse("2020-01-01T00:00:00Z")

    val bloodSugarNow = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("375cc86f-c582-43dd-aa0f-c06d73ea954b"),
        patientUuid = patientUuid,
        createdAt = createdAt,
        recordedAt = recordedAt
    )

    val bloodSugar15MinutesInPast = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("6495f97d-b1a9-42f6-9fb5-8d267e3e0633"),
        patientUuid = patientUuid,
        createdAt = createdAt.minus(15, ChronoUnit.MINUTES),
        recordedAt = recordedAt.minus(1, ChronoUnit.DAYS)
    )

    val bloodSugars: PagingData<BloodSugarHistoryListItem> = PagingData.from(listOf(
        BloodSugarHistoryListItem.BloodSugarHistoryItem(
            measurement = bloodSugarNow,
            isBloodSugarEditable = true,
            bloodSugarUnitPreference = BloodSugarUnitPreference.Mg,
            bloodSugarDate = "1-Jan-2020",
            bloodSugarTime = null
        ),
        BloodSugarHistoryListItem.BloodSugarHistoryItem(
            measurement = bloodSugar15MinutesInPast,
            bloodSugarUnitPreference = BloodSugarUnitPreference.Mg,
            isBloodSugarEditable = false,
            bloodSugarDate = "31-Dec-2019",
            bloodSugarTime = "12:00 AM"
        ),
    ))

    updateSpec
        .given(defaultModel)
        .whenEvent(BloodSugarHistoryLoaded(bloodSugars))
        .then(assertThatNext(
            hasModel(defaultModel.bloodSugarLoaded(bloodSugars)),
            hasNoEffects()
        ))
  }
}
