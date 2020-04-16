package org.simple.clinic.selectcountry

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.clinic.appconfig.AppConfigRepository
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.FetchError
import org.simple.clinic.appconfig.FetchSucceeded
import org.simple.clinic.appconfig.ManifestFetchResult
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.util.ResolvedError.NetworkRelated
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.net.URI

class SelectCountryEffectHandlerTest {

  private val repository = mock<AppConfigRepository>()
  private val uiActions = mock<UiActions>()

  private val india = Country(
      isoCountryCode = "IN",
      endpoint = URI("https://in.simple.org"),
      displayName = "India",
      isdCode = "91"
  )

  private val effectHandler = SelectCountryEffectHandler.create(repository, uiActions, TrampolineSchedulersProvider())
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
    verifyZeroInteractions(uiActions)
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
    verifyZeroInteractions(uiActions)
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
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the save country effect is received, the country must be saved locally`() {
    // given
    whenever(repository.saveCurrentCountry(india)) doReturn Completable.complete()

    // when
    testCase.dispatch(SaveCountryEffect(india))

    // then
    verify(repository).saveCurrentCountry(india)
    verifyNoMoreInteractions(repository)
    testCase.assertOutgoingEvents(CountrySaved)
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the go to next screen effect is received, the next screen must be opened`() {
    // when
    testCase.dispatch(GoToNextScreen)

    // then
    verifyZeroInteractions(repository)
    testCase.assertNoOutgoingEvents()
    verify(uiActions).goToNextScreen()
    verifyNoMoreInteractions(uiActions)
  }
}
