package org.simple.clinic.user.finduser

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.registration.RegistrationApi
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.finduser.FindUserResult.*
import org.simple.clinic.util.RxErrorsRule
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.UUID

class UserLookupTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val phoneNumber = "1234567890"

  private val registrationApi = mock<RegistrationApi>()

  private val findUserWithPhoneNumber = UserLookup(registrationApi)

  @Test
  fun `when the find user call is successful, the user payload must be returned in the result`() {
    // given
    val loggedInUserPayload = PatientMocker.loggedInUserPayload(uuid = UUID.fromString("f61b59da-4e2c-4e3f-a33f-afcd45022bbc"), phone = phoneNumber)

    whenever(registrationApi.findUser(phoneNumber)) doReturn Single.just(loggedInUserPayload)

    // when
    val result = findUserWithPhoneNumber.find(phoneNumber).blockingGet()

    // then
    assertThat(result).isEqualTo(Found(loggedInUserPayload))
  }

  @Test
  fun `when the find user call fails with http 404, the not found result must be returned`() {
    // given
    whenever(registrationApi.findUser(phoneNumber)) doReturn Single.error(httpException(404))

    // when
    val result = findUserWithPhoneNumber.find(phoneNumber).blockingGet()

    // then
    assertThat(result).isEqualTo(NotFound)
  }

  @Test
  fun `when the find user call fails with a network error, the network error result must be returned`() {
    // given
    whenever(registrationApi.findUser(phoneNumber)) doReturn Single.error(IOException())

    // when
    val result = findUserWithPhoneNumber.find(phoneNumber).blockingGet()

    // then
    assertThat(result).isEqualTo(NetworkError)
  }

  @Test
  fun `when the find user call fails with any other error, the unexpected error result must be returned`() {
    // given
    whenever(registrationApi.findUser(phoneNumber)) doReturn Single.error(httpException(500))

    // when
    val result = findUserWithPhoneNumber.find(phoneNumber).blockingGet()

    // then
    assertThat(result).isEqualTo(UnexpectedError)
  }

  private fun httpException(code: Int): HttpException {
    val error = Response.error<LoggedInUserPayload>(code, ResponseBody.create(MediaType.parse("text"), ""))

    return HttpException(error)
  }
}
