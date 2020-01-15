package org.simple.clinic.editpatient

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import org.junit.After
import org.junit.Test
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class EditPatientEffectHandlerTest {

  private val date = LocalDate.parse("2018-01-01")
  private val ui = mock<EditPatientUi>()
  private val userClock = TestUserClock(date)
  private val utcClock = TestUtcClock(Instant.parse("2018-01-01T00:00:00Z"))
  private val patientRepository = mock<PatientRepository>()
  private val dateOfBirthFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

  private val patientAddress = PatientMocker.address(uuid = UUID.fromString("85d0b5f1-af84-4a6b-938e-5166f8c27666"))
  private val patient = PatientMocker.patient(
      uuid = UUID.fromString("c9c9d4db-cd80-4b67-bf69-378de9656b49"),
      addressUuid = patientAddress.uuid,
      age = null,
      dateOfBirth = LocalDate.now(userClock).minusYears(30)
  )
  private val phoneNumber = PatientMocker.phoneNumber(
      uuid = UUID.fromString("61638775-2815-4f59-b513-643cc2fe3c90"),
      patientUuid = patient.uuid
  )
  private val entry = EditablePatientEntry.from(
      patient = patient,
      address = patientAddress,
      phoneNumber = phoneNumber,
      dateOfBirthFormatter = dateOfBirthFormatter
  )

  private val effectHandler = EditPatientEffectHandler(
      ui = ui,
      userClock = userClock,
      patientRepository = patientRepository,
      utcClock = utcClock,
      dateOfBirthFormatter = dateOfBirthFormatter,
      schedulersProvider = TrampolineSchedulersProvider()
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  // This is a temporary test added in order to hotfix a potential bug. If we allow the user to save
  // a blank Identifier, this would cause the sync for the associated patient to fail and would prevent
  // edits from syncing.
  //
  // This feature has to change so that clearing the text field should instead delete the existing
  // alternate identifier.
  // TODO(vs): 2020-01-15 Change feature to soft-delete existing Bangladesh ID
  @Test
  fun `editing a patient with a blank bangladesh ID should not save the business ID`() {
    // given
    val blankBangladeshNationalId = PatientMocker.businessId(
        uuid = UUID.fromString("77bd5387-641b-42f8-ab8d-d662bcee9b00"),
        patientUuid = patient.uuid,
        identifier = Identifier(value = "", type = Identifier.IdentifierType.BangladeshNationalId)
    )
    whenever(patientRepository.updatePatient(patient)) doReturn Completable.complete()
    whenever(patientRepository.updateAddressForPatient(patient.uuid, patientAddress)) doReturn Completable.complete()
    whenever(patientRepository.updatePhoneNumberForPatient(patient.uuid, phoneNumber)) doReturn Completable.complete()
    whenever(patientRepository.saveBusinessId(blankBangladeshNationalId)) doReturn Completable.complete()

    // when
    testCase.dispatch(SavePatientEffect(entry, patient, patientAddress, phoneNumber, blankBangladeshNationalId))

    // then
    verify(patientRepository).updatePatient(patient)
    verify(patientRepository).updateAddressForPatient(patient.uuid, patientAddress)
    verify(patientRepository).updatePhoneNumberForPatient(patient.uuid, phoneNumber)
    verify(patientRepository, never()).saveBusinessId(any())
    verifyNoMoreInteractions(patientRepository)
    testCase.assertOutgoingEvents(PatientSaved)
    verifyZeroInteractions(ui)
  }

  // This is a temporary test added in order to hotfix a potential bug. If we allow the user to save
  // a blank Identifier, this would cause the sync for the associated patient to fail and would prevent
  // edits from syncing.
  //
  // TODO(vs): 2020-01-15 Change feature to soft-delete existing Bangladesh ID
  @Test
  fun `editing a patient with a null bangladesh ID should not save the business ID`() {
    // given
    whenever(patientRepository.updatePatient(patient)) doReturn Completable.complete()
    whenever(patientRepository.updateAddressForPatient(patient.uuid, patientAddress)) doReturn Completable.complete()
    whenever(patientRepository.updatePhoneNumberForPatient(patient.uuid, phoneNumber)) doReturn Completable.complete()

    // when
    testCase.dispatch(SavePatientEffect(entry, patient, patientAddress, phoneNumber, null))

    // then
    verify(patientRepository).updatePatient(patient)
    verify(patientRepository).updateAddressForPatient(patient.uuid, patientAddress)
    verify(patientRepository).updatePhoneNumberForPatient(patient.uuid, phoneNumber)
    verify(patientRepository, never()).saveBusinessId(any())
    verifyNoMoreInteractions(patientRepository)
    testCase.assertOutgoingEvents(PatientSaved)
    verifyZeroInteractions(ui)
  }

  @Test
  fun `editing a patient with a non-blank bangladesh ID should save the business ID`() {
    // given
    val bangladeshNationalId = PatientMocker.businessId(
        uuid = UUID.fromString("77bd5387-641b-42f8-ab8d-d662bcee9b00"),
        patientUuid = patient.uuid,
        identifier = Identifier(value = "1234567890abcd", type = Identifier.IdentifierType.BangladeshNationalId)
    )
    whenever(patientRepository.updatePatient(patient)) doReturn Completable.complete()
    whenever(patientRepository.updateAddressForPatient(patient.uuid, patientAddress)) doReturn Completable.complete()
    whenever(patientRepository.updatePhoneNumberForPatient(patient.uuid, phoneNumber)) doReturn Completable.complete()
    whenever(patientRepository.saveBusinessId(bangladeshNationalId)) doReturn Completable.complete()

    // when
    testCase.dispatch(SavePatientEffect(entry, patient, patientAddress, phoneNumber, bangladeshNationalId))

    // then
    verify(patientRepository).updatePatient(patient)
    verify(patientRepository).updateAddressForPatient(patient.uuid, patientAddress)
    verify(patientRepository).updatePhoneNumberForPatient(patient.uuid, phoneNumber)
    verify(patientRepository).saveBusinessId(bangladeshNationalId)
    verifyNoMoreInteractions(patientRepository)
    testCase.assertOutgoingEvents(PatientSaved)
    verifyZeroInteractions(ui)
  }
}
