package experiments

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody

class ExperimentsServer: Interceptor {

  override fun intercept(chain: Interceptor.Chain): Response {
    return Response
        .Builder()
        .request(chain.request())
        .protocol(Protocol.HTTP_1_1)
        .code(500)
        .message("Internal Server Error")
        .body(
            ResponseBody.create(MediaType.parse("application/json"), "")
        )
        .build()
  }
}
