package org.simple.clinic.selectstate

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.util.ResolvedError.ServerError
import retrofit2.HttpException
import retrofit2.Response

class SelectStateUpdateTest {

  private val updateSpec = UpdateSpec(SelectStateUpdate())
  private val defaultModel = SelectStateModel.create()

  @Test
  fun `when states are fetched successfully, then update the ui`() {
    val states = listOf(
        TestData.state(displayName = "Andhra Pradesh"),
        TestData.state(displayName = "Kerala")
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(StatesFetched(states))
        .then(assertThatNext(
            hasModel(defaultModel.statesLoaded(states)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when fetching states fails, then update the ui`() {
    val cause = serverError()
    val error = StatesFetchError.fromResolvedError(ServerError(cause))

    updateSpec
        .given(defaultModel)
        .whenEvent(FailedToFetchStates(error))
        .then(assertThatNext(
            hasModel(defaultModel.failedToLoadStates(StatesFetchError.ServerError)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when state is saved, then go to registration screen`() {
    val andhraPradesh = TestData.state(displayName = "Andhra Pradesh")
    val kerala = TestData.state(displayName = "Kerala")
    val states = listOf(andhraPradesh, kerala)
    val statesLoadedModel = defaultModel
        .statesLoaded(states)
        .stateChanged(andhraPradesh)

    updateSpec
        .given(statesLoadedModel)
        .whenEvent(StateSaved)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToRegistrationScreen)
        ))
  }

  @Test
  fun `when retry button is clicked, then load states`() {
    val cause = serverError()
    val error = StatesFetchError.fromResolvedError(ServerError(cause))
    val failedToLoadStatesModel = defaultModel
        .failedToLoadStates(error)

    updateSpec
        .given(defaultModel)
        .whenEvent(RetryButtonClicked)
        .then(assertThatNext(
            hasModel(failedToLoadStatesModel.loadingStates()),
            hasEffects(LoadStates)
        ))
  }

  private fun serverError(): HttpException {
    val response = Response.error<String>(
        401,
        "FAIL".toResponseBody("text/plain".toMediaType())
    )
    return HttpException(response)
  }
}
