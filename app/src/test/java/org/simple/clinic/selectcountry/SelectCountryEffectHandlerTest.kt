package org.simple.clinic.selectcountry

import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.appconfig.FetchError
import org.simple.clinic.appconfig.FetchSucceeded
import org.simple.clinic.appconfig.ManifestFetchResult
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.ResolvedError.NetworkRelated
import org.simple.clinic.util.scheduler.TestSchedulersProvider

class SelectCountryEffectHandlerTest {

  private val repository = mock<AppConfigRepository>()
  private val uiActions = mock<UiActions>()

  private val india = TestData.country(
      isoCountryCode = "IN",
      deploymentEndPoint = "https://in.simple.org",
      displayName = "India",
      isdCode = "91"
  )

  private val viewEffectHandler = SelectCountryViewEffectHandler(uiActions)

  private val effectHandler = SelectCountryEffectHandler(
      repository,
      TestSchedulersProvider.trampoline(),
      viewEffectHandler::handle
  ).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `the app manifest must be fetched when the fetch manifest effect is received`() {
    // given
    whenever(repository.fetchAppManifest()) doReturn Single.never<ManifestFetchResult>()

    // when
    testCase.dispatch(FetchManifest)

    // then
    verify(repository).fetchAppManifest()
    verifyNoMoreInteractions(repository)
    testCase.assertNoOutgoingEvents()
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when fetching the app manifest succeeds, the manifest fetched event must be emitted`() {
    // given
    val countries = listOf(india)
    whenever(repository.fetchAppManifest()) doReturn Single.just<ManifestFetchResult>(FetchSucceeded(countries))

    // when
    testCase.dispatch(FetchManifest)

    // then
    testCase.assertOutgoingEvents(ManifestFetched(countries))
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when fetching the app manifest fails, the manifest fetch failed event must be emitted`() {
    // given
    val error = NetworkRelated(RuntimeException())
    whenever(repository.fetchAppManifest()) doReturn Single.just<ManifestFetchResult>(FetchError(error))

    // when
    testCase.dispatch(FetchManifest)

    // then
    testCase.assertOutgoingEvents(ManifestFetchFailed(NetworkError))
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when the save country effect is received, the country must be saved locally`() {
    // when
    testCase.dispatch(SaveCountryEffect(india))

    // then
    verify(repository).saveCurrentCountry(india)
    verifyNoMoreInteractions(repository)
    testCase.assertOutgoingEvents(CountrySaved)
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when the go to state selection screen effect is received, then go to state selection screen`() {
    // when
    testCase.dispatch(GoToStateSelectionScreen)

    // then
    verifyNoInteractions(repository)
    testCase.assertNoOutgoingEvents()
    verify(uiActions).goToStateSelectionScreen()
    verifyNoMoreInteractions(uiActions)
  }
}
