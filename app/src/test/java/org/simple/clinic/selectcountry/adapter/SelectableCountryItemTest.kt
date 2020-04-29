package org.simple.clinic.selectcountry.adapter

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.displayname.CountryDisplayNameFetcher
import java.net.URI

class SelectableCountryItemTest {

  private val india = Country(
      isoCountryCode = "IN",
      endpoint = URI("https://in.simple.org"),
      displayName = "India",
      isdCode = "91"
  )
  private val bangladesh = Country(
      isoCountryCode = "BD",
      endpoint = URI("https://bd.simple.org"),
      displayName = "Bangladesh",
      isdCode = "880"
  )
  private val ethiopia = Country(
      isoCountryCode = "ET",
      endpoint = URI("https://et.simple.org"),
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
