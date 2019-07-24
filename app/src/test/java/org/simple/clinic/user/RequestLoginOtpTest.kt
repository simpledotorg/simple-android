package org.simple.clinic.user

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.login.LoginApi
import org.simple.clinic.login.LoginOtpSmsListener
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class RequestLoginOtpTest {

  private val userUuid = UUID.fromString("d3899412-c266-4abd-9ce8-772ccb65ef45")
  private val loginApi = mock<LoginApi>()
  private val loginOtpSmsListener = mock<LoginOtpSmsListener>()

  private val requestLoginOtp = RequestLoginOtp(loginApi, loginOtpSmsListener)

  @Before
  fun setUp() {
    whenever(loginOtpSmsListener.listenForLoginOtp())
        .thenReturn(Completable.complete())
  }

  @Test
  fun `when the OTP is requested and the api call is successful, the result should be Success`() {
    // given
    whenever(loginApi.requestLoginOtp(userUuid))
        .thenReturn(Completable.complete())

    // then
    requestLoginOtp
        .requestForUser(userUuid)
        .test()
        .assertValue(RequestLoginOtp.Result.Success)
  }

  @Test
  @Parameters(value = ["400", "401", "500"])
  fun `when the OTP is requested and the api call fails with a response code, the result should be ServerError`(
      responseCode: Int
  ) {
    // given
    val httpException = createHttpException(responseCode)
    whenever(loginApi.requestLoginOtp(userUuid))
        .thenReturn(Completable.error(httpException))

    // then
    requestLoginOtp
        .requestForUser(userUuid)
        .test()
        .assertValue(RequestLoginOtp.Result.ServerError(responseCode))
  }

  @Test
  @Parameters(method = "params for network errors in request otp call")
  fun `when the OTP is requested and the api call fails with an IOException, the result should be NetworkError`(
      cause: IOException
  ) {
    // given
    whenever(loginApi.requestLoginOtp(userUuid))
        .thenReturn(Completable.error(cause))

    // then
    requestLoginOtp
        .requestForUser(userUuid)
        .test()
        .assertValue(RequestLoginOtp.Result.NetworkError)
  }

  @Suppress("Unused", "RemoveExplicitTypeArguments")
  private fun `params for network errors in request otp call`() = listOf<IOException>(
      IOException(),
      SocketTimeoutException(),
      SocketException()
  )

  @Test
  @Parameters(method = "params for errors in request otp call")
  fun `when the OTP is requested and the api call fails with any error apart from IOException, the result should be OtherError`(
      cause: Throwable
  ) {
    // given
    whenever(loginApi.requestLoginOtp(userUuid))
        .thenReturn(Completable.error(cause))

    // then
    requestLoginOtp
        .requestForUser(userUuid)
        .test()
        .assertValue(RequestLoginOtp.Result.OtherError(cause))
  }

  @Suppress("Unused")
  private fun `params for errors in request otp call`() = listOf<Throwable>(
      RuntimeException(),
      NullPointerException(),
      IllegalStateException()
  )

  @Test
  fun `when the OTP is requested, the otp sms listener must be setup`() {
    // given
    whenever(loginApi.requestLoginOtp(userUuid))
        .thenReturn(Completable.complete())
    var listenerSetup = false
    whenever(loginOtpSmsListener.listenForLoginOtp())
        .thenReturn(Completable.complete().doOnSubscribe { listenerSetup = true })

    // when
    requestLoginOtp
        .requestForUser(userUuid)
        .toCompletable()
        .blockingAwait()

    // then
    assertThat(listenerSetup).isTrue()
  }

  @Test
  fun `when the OTP is requested and the otp sms listener fails, the login otp request network call must be made`() {
    // given
    whenever(loginApi.requestLoginOtp(userUuid)).thenReturn(Completable.complete())
    whenever(loginOtpSmsListener.listenForLoginOtp()).thenReturn(Completable.error(RuntimeException()))

    // then
    requestLoginOtp
        .requestForUser(userUuid)
        .test()
        .assertValue(RequestLoginOtp.Result.Success)
  }

  private fun createHttpException(responseCode: Int): HttpException = HttpException(
      Response.error<String>(
          responseCode,
          ResponseBody.create(MediaType.parse("text/plain"), "")
      )
  )
}
