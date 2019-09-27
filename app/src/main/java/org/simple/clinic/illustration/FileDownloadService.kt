package org.simple.clinic.illustration

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface FileDownloadService {

  @Streaming
  @GET
  fun downloadFile(@Url fileUrl: String): Observable<ResponseBody>
}
