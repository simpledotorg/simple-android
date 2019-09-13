package experiments

import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.simple.clinic.BuildConfig
import org.simple.clinic.facility.FacilityPullResponse
import org.simple.clinic.login.LoginResponse
import org.simple.clinic.user.LoggedInUserPayload

class ExperimentsServer(moshi: Moshi) : Interceptor {

  private val loggedInUserPayloadAdapter = moshi.adapter(LoggedInUserPayload::class.java)
  private val facilityPullResponseAdapter = moshi.adapter(FacilityPullResponse::class.java)
  private val loginResponsePayloadAdapter = moshi.adapter(LoginResponse::class.java)

  private val json = MediaType.parse("application/json")
  private val text = MediaType.parse("text/plain")

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = chain.request()
    return Response
        .Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .apply { setResponse(request, this) }
        .build()
  }

  private fun setResponse(request: Request, builder: Response.Builder) {
    val url = request.url().toString()
    val method = request.method()

    val startIndex = "${BuildConfig.API_ENDPOINT}/v3/".lastIndex
    val endIndex = if (url.contains('?')) url.indexOf('?') else url.lastIndex + 1
    val resource = url.substring(startIndex, endIndex)

    when(method) {
      "GET" -> {
        when (resource) {
          "users/find" -> builder.loggedInUser()
          "facilities/sync" -> builder.facilities()
          else -> builder.serverError()
        }
      }
      "POST" -> {
        when (resource) {
          "users/${ExperimentData.loggedInUserPayload.uuid}/request_otp" -> builder.ok()
          "login" -> builder.loginResponse()
        }
      }
      else -> builder.serverError()
    }

  }

  private fun Response.Builder.loggedInUser(): Response.Builder {
    code(200).message("OK")

    body(ResponseBody.create(json, loggedInUserPayloadAdapter.toJson(ExperimentData.loggedInUserPayload)))

    return this
  }

  private fun Response.Builder.loginResponse(): Response.Builder {
    code(200).message("OK")

    val loginResponse = LoginResponse(
        accessToken = "token",
        loggedInUser = ExperimentData.loggedInUserPayload
    )
    body(ResponseBody.create(json, loginResponsePayloadAdapter.toJson(loginResponse)))

    return this
  }

  private fun Response.Builder.facilities(): Response.Builder {
    code(200).message("OK")

    val facilityPullResponse = FacilityPullResponse(ExperimentData.facilityPayload, processToken = "token")
    body(ResponseBody.create(json, facilityPullResponseAdapter.toJson(facilityPullResponse)))

    return this
  }

  private fun Response.Builder.ok(): Response.Builder {
    code(200)
        .message("OK")
        .body(ResponseBody.create(text, ""))

    return this
  }

  private fun Response.Builder.serverError(): Response.Builder {
    code(500)
        .message("Internal Server Error")
        .body(ResponseBody.create(text, ""))

    return this
  }
}
