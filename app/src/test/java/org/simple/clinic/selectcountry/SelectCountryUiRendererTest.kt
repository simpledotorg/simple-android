package org.simple.clinic.selectcountry

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData

class SelectCountryUiRendererTest {

  private val defaultModel = SelectCountryModel.FETCHING
  private val ui = mock<SelectCountryUi>()

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
  private val countries = listOf(india, bangladesh)

  private val renderer = SelectCountryUiRenderer(ui)

  @Test
  fun `when the model is being initialized, show the progress bar`() {
    // when
    renderer.render(defaultModel)

    // then
    verify(ui).showProgress()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the manifest has been fetched, render the list of countries`() {
    // given
    val model = defaultModel.manifestFetched(countries)

    // when
    renderer.render(model)

    // then
    verify(ui).displaySupportedCountries(countries, null)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the manifest fails to fetch with a network error, display the network error message and show the retry button`() {
    // given
    val model = defaultModel.manifestFetchError(NetworkError)

    // when
    renderer.render(model)

    // then
    verify(ui).displayNetworkErrorMessage()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the manifest fails to fetch with a server error, display the server error message and show the retry button`() {
    // given
    val model = defaultModel.manifestFetchError(ServerError)

    // when
    renderer.render(model)

    // then
    verify(ui).displayServerErrorMessage()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the manifest fails to fetch with any other error, display a generic error message and show the retry button`() {
    // given
    val model = defaultModel.manifestFetchError(UnexpectedError)

    // when
    renderer.render(model)

    // then
    verify(ui).displayGenericErrorMessage()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when fetching the manifest is being retried, show the progress bar`() {
    // given
    val model = defaultModel
        .manifestFetchError(NetworkError)
        .fetching()

    // when
    renderer.render(model)

    // then
    verify(ui).showProgress()
    verifyNoMoreInteractions(ui)
  }
}
