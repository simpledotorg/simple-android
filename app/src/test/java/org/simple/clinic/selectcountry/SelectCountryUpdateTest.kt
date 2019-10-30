package org.simple.clinic.selectcountry

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import java.net.URI

class SelectCountryUpdateTest {

  private val defaultModel = SelectCountryModel.FETCHING

  private val spec = UpdateSpec(SelectCountryUpdate())

  val india = Country(
      code = "IN",
      endpoint = URI("https://in.simple.org"),
      displayName = "India",
      isdCode = "91"
  )

  private val bangladesh = Country(
      code = "BD",
      endpoint = URI("https://bd.simple.org"),
      displayName = "Bangladesh",
      isdCode = "880"
  )

  private val countries = listOf(india, bangladesh)

  @Test
  fun `when the manifest is fetched, then update the countries list`() {
    spec
        .given(defaultModel)
        .whenEvent(ManifestFetched(countries))
        .then(assertThatNext(
            hasModel(defaultModel.manifestFetched(countries)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the manifest fetch fails, then update the error`() {
    spec
        .given(defaultModel)
        .whenEvent(ManifestFetchFailed(NetworkError))
        .then(assertThatNext(
            hasModel(defaultModel.manifestFetchError(NetworkError)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when country has been chosen, then update the selected country`() {
    val model = defaultModel
        .manifestFetched(countries)

    spec
        .given(model)
        .whenEvent(CountryChosen(bangladesh))
        .then(assertThatNext(
            hasModel(model.countryChosen(bangladesh)),
            hasNoEffects()
        ))
  }
}
