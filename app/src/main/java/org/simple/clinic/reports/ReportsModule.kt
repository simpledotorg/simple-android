package org.simple.clinic.reports

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.Request
import org.simple.clinic.user.UserSession
import retrofit2.Retrofit
import javax.inject.Named

@Module
class ReportsModule {
  @Provides
  fun reportsApi(@Named("for_country") retrofit: Retrofit): ReportsApi = retrofit.create(ReportsApi::class.java)

  @Provides
  @Named("reports_file_path")
  fun reportsFilePath() = "report.html"

  @Provides
  fun webViewClient(
      userSession: UserSession,
      okHttpClient: OkHttpClient
  ): WebViewClient {
    val accessToken = userSession.accessToken().get()
    val userDetails = userSession.userFacilityDetails()!!

    return object : WebViewClient() {
      override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        try {
          val url = request?.url?.toString() ?: return null
          val okHttpRequest = Request.Builder()
              .url(url)
              .addHeader("Authorization", "Bearer $accessToken")
              .addHeader("X-USER-ID", userDetails.userId.toString())
              .addHeader("X-FACILITY-ID", userDetails.currentFacilityId.toString())
              .addHeader("X-SYNC-REGION-ID", userDetails.currentSyncGroupId)
              .build()

          val response = okHttpClient.newCall(okHttpRequest).execute()
          return WebResourceResponse("text", "utf-8", response.body()?.byteStream())
        } catch (e: Exception) {
          return null
        }
      }
    }
  }
}
