package org.simple.clinic.drugs.search

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.drugs.search.DrugSearchListItem.BottomCornerCapItem
import org.simple.clinic.drugs.search.DrugSearchListItem.Divider
import org.simple.clinic.drugs.search.DrugSearchListItem.DrugSearchResult
import org.simple.clinic.drugs.search.DrugSearchListItem.NewCustomDrug
import org.simple.clinic.drugs.search.DrugSearchListItem.TopCornerCapItem
import org.simple.clinic.drugs.selection.custom.drugfrequency.country.DrugFrequencyChoiceItem
import java.util.UUID

class DrugSearchListItemTest {

  private val drugFrequencyToFrequencyChoiceItemMap = mapOf(
      null to DrugFrequencyChoiceItem(drugFrequency = null, label = "None"),
      DrugFrequency.OD to DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.OD, label = "OD"),
      DrugFrequency.BD to DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.BD, label = "BD"),
      DrugFrequency.TDS to DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.TDS, label = "TDS"),
      DrugFrequency.QDS to DrugFrequencyChoiceItem(drugFrequency = DrugFrequency.QDS, label = "QDS")
  )

  @Test
  fun `divider must be added in between drug search results`() {
    // given
    val amlodipine10 = DrugSearchResult(TestData.drug(
        id = UUID.fromString("6fecac0f-fe87-4283-b6d9-7a69be1de5af"),
        name = "Amlodipine",
        dosage = "10 mg"
    ), drugFrequencyToFrequencyChoiceItemMap)

    val amlodipine20 = DrugSearchResult(TestData.drug(
        id = UUID.fromString("494e0237-0f42-4c61-9d62-77aaa68916af"),
        name = "Amlodipine",
        dosage = "20 mg"
    ), drugFrequencyToFrequencyChoiceItemMap)

    // when
    val divider = DrugSearchListItem.insertSeparators(
        oldItem = amlodipine10,
        newItem = amlodipine20,
        searchQuery = "amlo"
    )

    // then
    assertThat(divider).isEqualTo(Divider)
  }

  @Test
  fun `when no drug search results are present, then add new custom drug item`() {
    // when
    val newCustomDrugItem = DrugSearchListItem.insertSeparators(
        oldItem = null,
        newItem = null,
        searchQuery = "Telmisartan"
    )

    // then
    assertThat(newCustomDrugItem).isEqualTo(NewCustomDrug("Telmisartan"))
  }

  @Test
  fun `add top corner cap item at the start of the list`() {
    // given
    val amlodipine10 = DrugSearchResult(TestData.drug(
        id = UUID.fromString("4c998e87-0bb2-4875-9cfe-c1c3efb625c6"),
        name = "Amlodipine",
        dosage = "10 mg"
    ), drugFrequencyToFrequencyChoiceItemMap)

    // when
    val topCornerCapItem = DrugSearchListItem.insertSeparators(
        oldItem = null,
        newItem = amlodipine10,
        searchQuery = "amlo"
    )

    // then
    assertThat(topCornerCapItem).isEqualTo(TopCornerCapItem)
  }

  @Test
  fun `add bottom corner cap item at the end of the list`() {
    // given
    val amlodipine10 = DrugSearchResult(TestData.drug(
        id = UUID.fromString("0871a787-049c-4f79-9726-61a30adbaeb4"),
        name = "Amlodipine",
        dosage = "10 mg"
    ), drugFrequencyToFrequencyChoiceItemMap)

    // when
    val bottomCornerCapItem = DrugSearchListItem.insertSeparators(
        oldItem = amlodipine10,
        newItem = null,
        searchQuery = "amlo"
    )

    // then
    assertThat(bottomCornerCapItem).isEqualTo(BottomCornerCapItem)
  }
}
