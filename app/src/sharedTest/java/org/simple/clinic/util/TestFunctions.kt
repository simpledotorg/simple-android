package org.simple.clinic.util

import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.TestScheduler
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.DeletedReason
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.User
import retrofit2.HttpException
import retrofit2.Response
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer as TeleconsultrecordAnswer

fun <T> TestObserver<T>.assertLatestValue(value: T) {
  @Suppress("UnstableApiUsage")
  assertValueAt(valueCount() - 1, value)
}

fun randomMedicalHistoryAnswer(): Answer {
  return Answer.TypeAdapter.knownMappings.keys.shuffled().first()
}

fun randomGender(): Gender {
  return Gender.TypeAdapter.knownMappings.keys.shuffled().first()
}

fun randomDeletedReason(): DeletedReason {
  return DeletedReason.TypeAdapter.knownMappings.keys.random()
}

fun randomPatientPhoneNumberType(): PatientPhoneNumberType {
  return PatientPhoneNumberType.TypeAdapter.knownMappings.keys.shuffled().first()
}

fun randomTeleconsultRecordAnswer(): TeleconsultrecordAnswer {
  return TeleconsultrecordAnswer.TypeAdapter.knownMappings.keys.shuffled().first()
}

fun randomTeleconsultationType(): TeleconsultationType {
  return TeleconsultationType.TypeAdapter.knownMappings.keys.shuffled().first()
}

fun TestScheduler.advanceTimeBy(duration: Duration) {
  advanceTimeBy(duration.toMillis(), TimeUnit.MILLISECONDS)
}

fun LoggedInUserPayload.toUser(loggedInStatus: User.LoggedInStatus): User {
  return User(
      uuid = uuid,
      fullName = fullName,
      phoneNumber = phoneNumber,
      pinDigest = pinDigest,
      status = status,
      createdAt = createdAt,
      updatedAt = updatedAt,
      loggedInStatus = loggedInStatus,
      registrationFacilityUuid = registrationFacilityId,
      currentFacilityUuid = registrationFacilityId,
      teleconsultPhoneNumber = teleconsultPhoneNumber
  )
}

fun User.toPayload(registrationFacilityUuid: UUID): LoggedInUserPayload {
  return LoggedInUserPayload(
      uuid = uuid,
      fullName = fullName,
      phoneNumber = phoneNumber,
      pinDigest = pinDigest,
      registrationFacilityId = registrationFacilityUuid,
      status = status,
      createdAt = createdAt,
      updatedAt = updatedAt,
      teleconsultPhoneNumber = teleconsultPhoneNumber
  )
}

fun httpErrorResponse(
    code: Int,
    contentType: String = "text/plain",
    body: String = ""
): HttpException {
  val error = Response.error<Any>(code, ResponseBody.create(MediaType.parse(contentType), body))
  return HttpException(error)
}

fun <T : Enum<T>> randomOfEnum(enumClass: KClass<T>): T {
  return enumClass.java.enumConstants!!.asList().shuffled().first()
}
