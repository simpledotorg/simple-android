package org.simple.clinic.selectcountry.adapter

import com.google.common.truth.Truth.assertThat
import org.mockito.kotlin.mock
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.appconfig.displayname.CountryDisplayNameFetcher

class SelectableCountryItemTest {

  private val india = TestData.country(
      isoCountryCode = "IN",
      deploymentEndPoint = "https://in.simple.org",
      displayName = "India",
      isdCode = "91"
  )
  private val bangladesh = TestData.country(
      isoCountryCode = "BD",
      deploymentEndPoint = "https://bd.simple.org",
      displayName = "Bangladesh",
      isdCode = "880"
  )
  private val ethiopia = TestData.country(
      isoCountryCode = "ET",
      deploymentEndPoint = "https://et.simple.org",
      displayName = "Ethiopia",
      isdCode = "251"
  )

  private val countries = listOf(india, bangladesh, ethiopia)
  private val countryDisplayNameFetcher = mock<CountryDisplayNameFetcher>()

  @Test
  fun `if the user has not chosen a country, none of the list items must be selected`() {
    // when
    val listItems = SelectableCountryItem.from(countries, null, countryDisplayNameFetcher)

    // then
    assertThat(listItems)
        .containsExactly(
            SelectableCountryItem(country = india, isCountryChosenByUser = false, showDivider = true, countryDisplayNameFetcher = countryDisplayNameFetcher),
            SelectableCountryItem(country = bangladesh, isCountryChosenByUser = false, showDivider = true, countryDisplayNameFetcher = countryDisplayNameFetcher),
            SelectableCountryItem(country = ethiopia, isCountryChosenByUser = false, showDivider = false, countryDisplayNameFetcher = countryDisplayNameFetcher)
        ).inOrder()
  }

  @Test
  fun `if the user has chosen a country, the corresponding list item must be selected`() {
    // when
    val listItems = SelectableCountryItem.from(countries, bangladesh, countryDisplayNameFetcher)

    // then
    assertThat(listItems)
        .containsExactly(
            SelectableCountryItem(country = india, isCountryChosenByUser = false, showDivider = true, countryDisplayNameFetcher = countryDisplayNameFetcher),
            SelectableCountryItem(country = bangladesh, isCountryChosenByUser = true, showDivider = true, countryDisplayNameFetcher = countryDisplayNameFetcher),
            SelectableCountryItem(country = ethiopia, isCountryChosenByUser = false, showDivider = false, countryDisplayNameFetcher = countryDisplayNameFetcher)
        ).inOrder()
  }

  @Test
  fun `if there is only one item in the list of supported countries, the divider must not be shown`() {
    // when
    val listItems = SelectableCountryItem.from(listOf(india), india, countryDisplayNameFetcher)

    // then
    assertThat(listItems)
        .containsExactly(SelectableCountryItem(country = india, isCountryChosenByUser = true, showDivider = false, countryDisplayNameFetcher = countryDisplayNameFetcher))
        .inOrder()
  }
}
