package org.simple.clinic.selectstate

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.StatesResult.FetchError
import org.simple.clinic.util.ResolvedError
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

    updateSpec
        .given(defaultModel)
        .whenEvent(StatesResultFetched(FetchError(ResolvedError.ServerError(cause))))
        .then(assertThatNext(
            hasModel(defaultModel.failedToLoadStates(StatesFetchError.ServerError)),
            hasNoEffects()
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
