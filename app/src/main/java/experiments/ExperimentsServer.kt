package experiments

import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.simple.clinic.BuildConfig
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.UserStatus
import org.threeten.bp.Instant
import java.util.UUID

class ExperimentsServer(moshi: Moshi): Interceptor {

  private val loggedInUserPayloadAdapter = moshi.adapter(LoggedInUserPayload::class.java)

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

    val startIndex = "${BuildConfig.API_ENDPOINT}/v3/".lastIndex
    val endIndex = if(url.contains('?')) url.indexOf('?') else url.lastIndex
    val resource = url.substring(startIndex, endIndex)
    when(resource) {
      "users/find" -> builder.loggedInUser()
      else -> builder.serverError()
    }
  }

  private fun Response.Builder.loggedInUser(): Response.Builder {
    code(200).message("OK")

    val loggedInUserPayload = LoggedInUserPayload(
        uuid = UUID.fromString("a9e10bef-0978-4363-9d4c-09587cb5805d"),
        fullName = "Test User",
        phoneNumber = "1111111111",
        pinDigest = "\$2a\$10\$HDDWyQS.SNtJ03QnubMBkeIDlLfpxXBQ0pgnuXmUvdtsSQVO4pgze",
        registrationFacilityId = UUID.fromString("9c422ef8-95f3-415b-a6be-0c04198a7411"),
        status = UserStatus.DisapprovedForSyncing,
        createdAt = Instant.parse("2018-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2018-01-01T00:00:00Z")
    )

    body(ResponseBody.create(json, loggedInUserPayloadAdapter.toJson(loggedInUserPayload)))

    return this
  }

  private fun Response.Builder.serverError(): Response.Builder {
    code(500)
        .message("Internal Server Error")
        .body(ResponseBody.create(text, ""))

    return this
  }
}
