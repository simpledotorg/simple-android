package org.simple.clinic.editpatient

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.util.toOptional
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class EditPatientEffectHandlerTest {

  private val date = LocalDate.parse("2018-01-01")
  private val ui = mock<EditPatientUi>()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userClock = TestUserClock(date)
  private val utcClock = TestUtcClock(Instant.parse("2018-01-01T00:00:00Z"))
  private val patientRepository = mock<PatientRepository>()
  private val country = TestData.country()
  private val dateOfBirthFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

  private val patientAddress = TestData.patientAddress(uuid = UUID.fromString("85d0b5f1-af84-4a6b-938e-5166f8c27666"))
  private val patient = TestData.patient(
      uuid = UUID.fromString("c9c9d4db-cd80-4b67-bf69-378de9656b49"),
      addressUuid = patientAddress.uuid,
      age = null,
      dateOfBirth = LocalDate.now(userClock).minusYears(30)
  )
  private val phoneNumber = TestData.patientPhoneNumber(
      uuid = UUID.fromString("61638775-2815-4f59-b513-643cc2fe3c90"),
      patientUuid = patient.uuid
  )

  private val bangladeshNationalId = TestData.businessId(
      uuid = UUID.fromString("77bd5387-641b-42f8-ab8d-d662bcee9b00"),
      patientUuid = patient.uuid,
      identifier = Identifier(value = "1234567890abcd", type = Identifier.IdentifierType.BangladeshNationalId)
  )

  private val entry = EditablePatientEntry.from(
      patient = patient,
      address = patientAddress,
      phoneNumber = phoneNumber,
      dateOfBirthFormatter = dateOfBirthFormatter,
      alternativeId = null
  )

  private val user = TestData.loggedInUser(uuid = UUID.fromString("3c3d0057-d6f6-42be-9bf6-5ccacb8bc54d"))
  private val facility = TestData.facility(uuid = UUID.fromString("d6685d51-f882-4995-b922-a6c637eed0a5"))

  private val effectHandler = EditPatientEffectHandler(
      ui = ui,
      userClock = userClock,
      patientRepository = patientRepository,
      utcClock = utcClock,
      schedulersProvider = TrampolineSchedulersProvider(),
      userSession = userSession,
      facilityRepository = facilityRepository,
      country = country,
      dateOfBirthFormatter = dateOfBirthFormatter
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `editing a patient with a blank bangladesh ID should delete the business ID`() {
    // given
    whenever(patientRepository.updatePatient(patient)) doReturn Completable.complete()
    whenever(patientRepository.updateAddressForPatient(patient.uuid, patientAddress)) doReturn Completable.complete()
    whenever(patientRepository.updatePhoneNumberForPatient(patient.uuid, phoneNumber)) doReturn Completable.complete()
    whenever(patientRepository.deleteBusinessId(bangladeshNationalId)) doReturn Completable.complete()

    // when
    testCase.dispatch(SavePatientEffect(entry.updateAlternativeId(""), patient, patientAddress, phoneNumber, bangladeshNationalId))

    // then
    verify(patientRepository).updatePatient(patient)
    verify(patientRepository).updateAddressForPatient(patient.uuid, patientAddress)
    verify(patientRepository).updatePhoneNumberForPatient(patient.uuid, phoneNumber)
    verify(patientRepository).deleteBusinessId(bangladeshNationalId)
    verify(patientRepository, never()).saveBusinessId(any())
    verify(patientRepository, never()).addIdentifierToPatient(any(), any(), any(), any())
    verifyNoMoreInteractions(patientRepository)
    testCase.assertOutgoingEvents(PatientSaved)
    verifyZeroInteractions(ui)
  }

  @Test
  fun `editing a patient with a null bangladesh ID should not save the business ID`() {
    // given
    whenever(patientRepository.updatePatient(patient)) doReturn Completable.complete()
    whenever(patientRepository.updateAddressForPatient(patient.uuid, patientAddress)) doReturn Completable.complete()
    whenever(patientRepository.updatePhoneNumberForPatient(patient.uuid, phoneNumber)) doReturn Completable.complete()
    whenever(patientRepository.deleteBusinessId(bangladeshNationalId)) doReturn Completable.complete()

    // when
    testCase.dispatch(SavePatientEffect(entry, patient, patientAddress, phoneNumber, bangladeshNationalId))

    // then
    verify(patientRepository).updatePatient(patient)
    verify(patientRepository).updateAddressForPatient(patient.uuid, patientAddress)
    verify(patientRepository).updatePhoneNumberForPatient(patient.uuid, phoneNumber)
    verify(patientRepository).deleteBusinessId(bangladeshNationalId)
    verify(patientRepository, never()).saveBusinessId(any())
    verify(patientRepository, never()).addIdentifierToPatient(any(), any(), any(), any())
    verifyNoMoreInteractions(patientRepository)
    testCase.assertOutgoingEvents(PatientSaved)
    verifyZeroInteractions(ui)
  }

  @Test
  fun `editing a patient with a non-blank bangladesh ID should save the business ID`() {
    // given
    val bangladeshNationalIdText = "1569273"
    val ongoingEntryWithBangladeshId = entry.updateAlternativeId(bangladeshNationalIdText)
    val updatedBangladeshNationalId = bangladeshNationalId.updateIdentifierValue(bangladeshNationalIdText)

    whenever(patientRepository.updatePatient(patient)) doReturn Completable.complete()
    whenever(patientRepository.updateAddressForPatient(patient.uuid, patientAddress)) doReturn Completable.complete()
    whenever(patientRepository.updatePhoneNumberForPatient(patient.uuid, phoneNumber)) doReturn Completable.complete()
    whenever(patientRepository.saveBusinessId(updatedBangladeshNationalId)) doReturn Completable.complete()

    // when
    testCase.dispatch(SavePatientEffect(ongoingEntryWithBangladeshId, patient, patientAddress, phoneNumber, bangladeshNationalId))

    // then
    verify(patientRepository).updatePatient(patient)
    verify(patientRepository).updateAddressForPatient(patient.uuid, patientAddress)
    verify(patientRepository).updatePhoneNumberForPatient(patient.uuid, phoneNumber)
    verify(patientRepository).saveBusinessId(updatedBangladeshNationalId)
    verify(patientRepository, never()).addIdentifierToPatient(any(), any(), any(), any())
    verifyNoMoreInteractions(patientRepository)
    testCase.assertOutgoingEvents(PatientSaved)
    verifyZeroInteractions(ui)
  }

  @Test
  fun `adding an id to an empty alternative id should create a new Business id if country has alternative id`() {
    //given
    val country = TestData.country(isoCountryCode = "BD")
    val effectHandler = EditPatientEffectHandler(
        ui = ui,
        userClock = userClock,
        patientRepository = patientRepository,
        utcClock = utcClock,
        schedulersProvider = TrampolineSchedulersProvider(),
        userSession = userSession,
        facilityRepository = facilityRepository,
        country = country,
        dateOfBirthFormatter = dateOfBirthFormatter
    )

    val testCase = EffectHandlerTestCase(effectHandler.build())
    val identifier = bangladeshNationalId.identifier

    whenever(patientRepository.updatePatient(patient)) doReturn Completable.complete()
    whenever(patientRepository.updateAddressForPatient(patient.uuid, patientAddress)) doReturn Completable.complete()
    whenever(patientRepository.updatePhoneNumberForPatient(patient.uuid, phoneNumber)) doReturn Completable.complete()
    whenever(patientRepository.saveBusinessId(bangladeshNationalId)) doReturn Completable.complete()
    whenever(userSession.loggedInUser()) doReturn (Observable.just(user.toOptional()))
    whenever(facilityRepository.currentFacility(user)) doReturn (Observable.just(facility))
    whenever(patientRepository.addIdentifierToPatient(patient.uuid, identifier, user, facility)) doReturn Single.just(bangladeshNationalId)

    //when
    testCase.dispatch(SavePatientEffect(
        entry.updateAlternativeId(identifier.value),
        patient,
        patientAddress,
        phoneNumber,
        null
    ))

    //then
    verify(patientRepository).updatePatient(patient)
    verify(patientRepository).updateAddressForPatient(patient.uuid, patientAddress)
    verify(patientRepository).updatePhoneNumberForPatient(patient.uuid, phoneNumber)
    verify(patientRepository).addIdentifierToPatient(patient.uuid, identifier, user, facility)
    verify(patientRepository, never()).saveBusinessId(any())
    verifyNoMoreInteractions(patientRepository)
    testCase.assertOutgoingEvents(PatientSaved)
    verifyZeroInteractions(ui)
  }
}
