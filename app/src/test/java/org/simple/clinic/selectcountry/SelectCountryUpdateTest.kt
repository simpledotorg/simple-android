package org.simple.clinic.selectcountry

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData

class SelectCountryUpdateTest {

  private val defaultModel = SelectCountryModel.FETCHING

  private val spec = UpdateSpec(SelectCountryUpdate())

  val india = TestData.country(
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
            hasEffects(SaveCountryEffect(bangladesh))
        ))
  }

  @Test
  fun `when retry is clicked, then fetch manifest`() {
    val model = defaultModel
        .manifestFetchError(NetworkError)

    spec
        .given(model)
        .whenEvent(RetryClicked)
        .then(assertThatNext(
            hasModel(model.fetching()),
            hasEffects(FetchManifest as SelectCountryEffect)
        ))
  }

  @Test
  fun `when selected country is saved and there is more than one deployment, then go to state selection screen`() {
    val ihci = TestData.deployment(
        endPoint = "https://in.simple.org",
        displayName = "IHCI"
    )
    val kerala = TestData.deployment(
        endPoint = "https://kerala.simple.org",
        displayName = "Kerala"
    )
    val india = TestData.country(
        isoCountryCode = "IN",
        displayName = "India",
        isdCode = "91",
        deployments = listOf(ihci, kerala)
    )
    val model = defaultModel
        .manifestFetched(countries)
        .countryChosen(india)

    spec
        .given(model)
        .whenEvent(CountrySaved)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToStateSelectionScreen)
        ))
  }
}
