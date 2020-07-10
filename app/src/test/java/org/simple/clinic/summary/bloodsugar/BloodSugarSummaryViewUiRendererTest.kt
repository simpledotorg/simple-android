package org.simple.clinic.summary.bloodsugar

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Test
import org.simple.clinic.TestData
import java.time.Duration
import java.util.UUID

class BloodSugarSummaryViewUiRendererTest {

  private val ui = mock<BloodSugarSummaryViewUi>()
  private val config = BloodSugarSummaryConfig(
      bloodSugarEditableDuration = Duration.ofSeconds(3600),
      numberOfBloodSugarsToDisplay = 3
  )
  private val renderer = BloodSugarSummaryViewUiRenderer(ui, config)
  private val patientUuid = UUID.fromString("9dd563b5-99a5-4f43-b3ab-47c43ed5d62c")
  private val defaultModel = BloodSugarSummaryViewModel.create(patientUuid)

  @Test
  fun `when blood sugar summary is being fetched then do nothing`() {
    //when
    renderer.render(defaultModel)

    //then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when blood sugar summary is fetched, then show it on the ui`() {
    //given
    val bloodSugars = listOf(TestData.bloodSugarMeasurement(UUID.fromString("6394f187-1e2b-454b-90bf-ed7bb55207ed")))

    //when
    renderer.render(defaultModel.summaryFetched(bloodSugars))

    //then
    verify(ui).showBloodSugarSummary(bloodSugars)
    verify(ui).hideSeeAllButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when blood sugar summary is empty then show empty ui state`() {
    //when
    renderer.render(defaultModel.summaryFetched(emptyList()))

    //then
    verify(ui).showNoBloodSugarsView()
    verify(ui).hideSeeAllButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show see all button if blood sugars count is more than number of blood sugars to display`() {
    // given
    val bloodSugar1 = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("0626681b-839a-4d68-a5ef-5ff6f592c236"),
        patientUuid = patientUuid
    )
    val bloodSugar2 = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("88a9c1b5-86eb-4ab8-941a-9c0ac69dd8c0"),
        patientUuid = patientUuid
    )
    val bloodSugar3 = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("55a15ccd-097e-407c-a6dc-f187b6442985"),
        patientUuid = patientUuid
    )
    val bloodSugar4 = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("89b290d9-896c-43bb-a8fb-1ed4997d9e25"),
        patientUuid = patientUuid
    )
    val bloodSugars = listOf(bloodSugar1, bloodSugar2, bloodSugar3, bloodSugar4)
    val bloodSugarsCount = bloodSugars.size

    // when
    renderer.render(
        defaultModel
            .summaryFetched(bloodSugars)
            .countFetched(bloodSugarsCount)
    )

    // then
    verify(ui).showBloodSugarSummary(bloodSugars)
    verify(ui).showSeeAllButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `hide see all button if there are less than or equal to number of blood sugars to display `() {
    // given
    val bloodSugar1 = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("e7c48875-3cfb-45f9-a155-08582c36983b"),
        patientUuid = patientUuid
    )
    val bloodSugar2 = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("f964ec33-b5d1-4ecd-9eb1-2c507432d9e4"),
        patientUuid = patientUuid
    )
    val bloodSugar3 = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("77880d0a-4a4a-4de6-bfca-2890b6212f4e"),
        patientUuid = patientUuid
    )
    val bloodSugars = listOf(bloodSugar1, bloodSugar2, bloodSugar3)
    val bloodSugarCount = bloodSugars.size

    // when
    renderer.render(
        defaultModel
            .summaryFetched(bloodSugars)
            .countFetched(bloodSugarCount)
    )

    // then
    verify(ui).showBloodSugarSummary(bloodSugars)
    verify(ui).hideSeeAllButton()
    verifyNoMoreInteractions(ui)
  }
}
