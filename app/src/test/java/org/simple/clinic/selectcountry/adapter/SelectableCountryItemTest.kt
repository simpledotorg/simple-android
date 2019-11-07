package org.simple.clinic.selectcountry.adapter

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.appconfig.Country
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

  @Test
  fun `if the user has not chosen a country, none of the list items must be selected`() {
    // when
    val listItems = SelectableCountryItem.from(countries, null)

    // then
    assertThat(listItems)
        .containsExactly(
            SelectableCountryItem(country = india, isSelected = false, showDivider = true),
            SelectableCountryItem(country = bangladesh, isSelected = false, showDivider = true),
            SelectableCountryItem(country = ethiopia, isSelected = false, showDivider = false)
        ).inOrder()
  }

  @Test
  fun `if the user has chosen a country, the corresponding list item must be selected`() {
    // when
    val listItems = SelectableCountryItem.from(countries, bangladesh)

    // then
    assertThat(listItems)
        .containsExactly(
            SelectableCountryItem(country = india, isSelected = false, showDivider = true),
            SelectableCountryItem(country = bangladesh, isSelected = true, showDivider = true),
            SelectableCountryItem(country = ethiopia, isSelected = false, showDivider = false)
        ).inOrder()
  }

  @Test
  fun `if there is only one item in the list of supported countries, the divider must not be shown`() {
    // when
    val listItems = SelectableCountryItem.from(listOf(india), india)

    // then
    assertThat(listItems)
        .containsExactly(SelectableCountryItem(country = india, isSelected = true, showDivider = false))
        .inOrder()
  }
}
