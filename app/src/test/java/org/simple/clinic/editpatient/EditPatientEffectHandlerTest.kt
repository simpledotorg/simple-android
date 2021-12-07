package org.simple.clinic.editpatient

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.Test
import org.simple.clinic.R
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.Country
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.newentry.form.AgeField
import org.simple.clinic.newentry.form.AlternativeIdInputField
import org.simple.clinic.newentry.form.DateOfBirthField
import org.simple.clinic.newentry.form.DistrictField
import org.simple.clinic.newentry.form.GenderField
import org.simple.clinic.newentry.form.LandlineOrMobileField
import org.simple.clinic.newentry.form.PatientNameField
import org.simple.clinic.newentry.form.StateField
import org.simple.clinic.newentry.form.StreetAddressField
import org.simple.clinic.newentry.form.VillageOrColonyField
import org.simple.clinic.newentry.form.ZoneField
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientAgeDetails
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.util.TestUserClock
import org.simple.clinic.util.TestUtcClock
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.util.toOptional
import org.simple.clinic.uuid.FakeUuidGenerator
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class EditPatientEffectHandlerTest {

  private val date = LocalDate.parse("2018-01-01")
  private val ui = mock<EditPatientUi>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userClock = TestUserClock(date)
  private val utcClock = TestUtcClock(Instant.parse("2018-01-01T00:00:00Z"))
  private val patientRepository = mock<PatientRepository>()
  private val dateOfBirthFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
  private val viewEffectHandler = EditPatientViewEffectHandler(ui)

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
      identifier = Identifier(value = "1234567890abcd", type = BangladeshNationalId)
  )

  private val india = TestData.country(isoCountryCode = Country.INDIA)
  private val bangladesh = TestData.country(isoCountryCode = Country.BANGLADESH)

  private val inputFields = InputFields(listOf(
      PatientNameField(R.string.patiententry_full_name),
      AgeField(R.string.patiententry_age),
      DateOfBirthField({ value -> LocalDate.parse(value, dateOfBirthFormatter) }, LocalDate.now(userClock), R.string.patiententry_date_of_birth_unfocused),
      LandlineOrMobileField(R.string.patiententry_phone_number),
      GenderField(_labelResId = 0, allowedGenders = setOf(Gender.Male, Gender.Female, Gender.Transgender)),
      AlternativeIdInputField(R.string.patiententry_bangladesh_national_id),
      StreetAddressField(R.string.patiententry_street_house_road_number),
      VillageOrColonyField(R.string.patiententry_village_ward),
      ZoneField(R.string.patiententry_zone),
      DistrictField(R.string.patiententry_upazila),
      StateField(R.string.patiententry_district)
  ))
  private val inputFieldsFactory = mock<InputFieldsFactory>()

  private val entry = EditablePatientEntry.from(
      patient = patient,
      address = patientAddress,
      phoneNumber = phoneNumber,
      dateOfBirthFormatter = dateOfBirthFormatter,
      alternativeId = null
  )

  private val user = TestData.loggedInUser(uuid = UUID.fromString("3c3d0057-d6f6-42be-9bf6-5ccacb8bc54d"))
  private val facility = TestData.facility(uuid = UUID.fromString("d6685d51-f882-4995-b922-a6c637eed0a5"))
  private val phoneNumberUuid = UUID.fromString("6bbc5bbe-863c-472a-b962-1fd3198e20d1")

  private val uuidGenerator = FakeUuidGenerator.fixed(phoneNumberUuid)

  private val effectHandler = EditPatientEffectHandler(
      patientRepository = patientRepository,
      utcClock = utcClock,
      schedulersProvider = TrampolineSchedulersProvider(),
      country = india,
      uuidGenerator = uuidGenerator,
      currentUser = Lazy { user },
      inputFieldsFactory = inputFieldsFactory,
      dateOfBirthFormatter = dateOfBirthFormatter,
      viewEffectsConsumer = viewEffectHandler::handle
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

    // TODO: 02/06/20 This is nasty, we need to fix the flow for registering patients
    // Tracked in the following tickets:
    // https://www.pivotaltracker.com/story/show/173122062
    // https://www.pivotaltracker.com/story/show/173122237
    val identifierUuid = UUID.fromString("a72c3ada-b071-4818-8f0b-476432338235")

    val effectHandler = EditPatientEffectHandler(
        patientRepository = patientRepository,
        utcClock = utcClock,
        schedulersProvider = TrampolineSchedulersProvider(),
        country = bangladesh,
        uuidGenerator = FakeUuidGenerator.fixed(identifierUuid),
        currentUser = dagger.Lazy { user },
        inputFieldsFactory = inputFieldsFactory,
        dateOfBirthFormatter = dateOfBirthFormatter,
        viewEffectsConsumer = viewEffectHandler::handle
    )

    val testCase = EffectHandlerTestCase(effectHandler.build())
    val identifier = bangladeshNationalId.identifier

    whenever(patientRepository.updatePatient(patient)) doReturn Completable.complete()
    whenever(patientRepository.updateAddressForPatient(patient.uuid, patientAddress)) doReturn Completable.complete()
    whenever(patientRepository.updatePhoneNumberForPatient(patient.uuid, phoneNumber)) doReturn Completable.complete()
    whenever(patientRepository.saveBusinessId(bangladeshNationalId)) doReturn Completable.complete()
    whenever(facilityRepository.currentFacility()) doReturn (Observable.just(facility))
    whenever(patientRepository.addIdentifierToPatient(
        uuid = identifierUuid,
        patientUuid = patient.uuid,
        identifier = identifier,
        assigningUser = user
    )) doReturn Single.just(bangladeshNationalId)

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
    verify(patientRepository).addIdentifierToPatient(
        uuid = identifierUuid,
        patientUuid = patient.uuid,
        identifier = identifier,
        assigningUser = user
    )
    verify(patientRepository, never()).saveBusinessId(any())
    verifyNoMoreInteractions(patientRepository)
    testCase.assertOutgoingEvents(PatientSaved)
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when fetch bp passports effect is received then all bp passports should be fetched`() {
    //given
    val bpPassport1 = TestData.businessId(
        uuid = UUID.fromString("f92e79df-7f45-4cdf-bebd-e449697083bf"),
        identifier = Identifier("8eae0ea0-eea4-453a-8b40-f7585301957a", BpPassport),
        patientUuid = patient.uuid
    )
    val bpPassport2 = TestData.businessId(
        uuid = UUID.fromString("c234c609-5780-48ae-a586-7749c2b62e8a"),
        identifier = Identifier("bcba83c5-8e96-4154-8376-c3f6f90eb27e", BpPassport),
        patientUuid = patient.uuid
    )

    val bangladeshId = TestData.businessId(
        uuid = UUID.fromString("13ab18c3-3ae1-4d6c-ba5a-418ea0c5a9d7"),
        identifier = Identifier("fd8441b6-d3cb-45bd-99da-e8aa448f3e72", BangladeshNationalId),
        patientUuid = patient.uuid
    )

    val patientProfile = TestData.patientProfile(
        patientUuid = patient.uuid,
        patientAddressUuid = patientAddress.uuid,
        generateBusinessId = false
    )
    val profileWithBusinessIds = patientProfile.copy(businessIds = listOf(bpPassport1, bpPassport2, bangladeshId))

    whenever(patientRepository.patientProfile(patient.uuid)) doReturn Observable.just(profileWithBusinessIds.toOptional())

    //when
    testCase.dispatch(FetchBpPassportsEffect(patient.uuid))

    //then
    testCase.assertOutgoingEvents(BpPassportsFetched(listOf(bpPassport1, bpPassport2)))
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when the load input fields effect is received, the input fields must be loaded`() {
    // given
    whenever(inputFieldsFactory.provideFields()) doReturn inputFields.fields

    // when
    testCase.dispatch(LoadInputFields)

    // then
    testCase.assertOutgoingEvents(InputFieldsLoaded(InputFields(inputFields.fields)))
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when fetch colony or village names effect is received, then load colony or village names`() {
    // given
    val colonyOrVillages = listOf("colony1", "colony2", "colony3", "colony4")
    whenever(patientRepository.allColoniesOrVillagesInPatientAddress()).thenReturn(colonyOrVillages)
    // when
    testCase.dispatch(FetchColonyOrVillagesEffect)

    //then
    testCase.assertOutgoingEvents(ColonyOrVillagesFetched(colonyOrVillages))
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when saving the patient, the recorded age must not be updated if the entered age is the same as the recorded age`() {
    // given
    val currentTime = Instant.now(utcClock)
    val patientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("6e7c5107-a762-453a-a5ef-b19c924f2f39"),
        generatePhoneNumber = false,
        generateBusinessId = false,
        age = Age(35, currentTime),
        dateOfBirth = null
    )
    val ongoingEntry = EditablePatientEntry.from(
        patient = patientProfile.patient,
        address = patientProfile.address,
        phoneNumber = patientProfile.phoneNumbers.firstOrNull(),
        dateOfBirthFormatter = dateOfBirthFormatter,
        alternativeId = null
    )
    whenever(patientRepository.updatePatient(patientProfile.patient)).thenReturn(Completable.complete())
    whenever(patientRepository.updateAddressForPatient(patientProfile.patientUuid, patientProfile.address)).thenReturn(Completable.complete())

    // when
    utcClock.advanceBy(Duration.ofSeconds(1))
    testCase.dispatch(SavePatientEffect(
        ongoingEntry = ongoingEntry,
        savedPatient = patientProfile.patient,
        savedAddress = patientProfile.address,
        savedPhoneNumber = patientProfile.phoneNumbers.firstOrNull(),
        saveAlternativeId = null
    ))

    // then
    testCase.assertOutgoingEvents(PatientSaved)
    verifyZeroInteractions(ui)
    verify(patientRepository).updatePatient(patientProfile.patient)
    verify(patientRepository).updateAddressForPatient(patientProfile.patientUuid, patientProfile.address)
    verifyNoMoreInteractions(patientRepository)
  }

  @Test
  fun `when saving the patient, the recorded age must be updated if the entered age is different from the recorded age`() {
    // given
    val currentTime = Instant.now(utcClock)
    val timeToAdvanceBy = Duration.ofSeconds(1)
    val recordedAge = 35
    val enteredAge = 37

    val patientProfile = TestData.patientProfile(
        patientUuid = UUID.fromString("6e7c5107-a762-453a-a5ef-b19c924f2f39"),
        generatePhoneNumber = false,
        generateBusinessId = false,
        age = Age(recordedAge, currentTime),
        dateOfBirth = null
    )
    val ongoingEntry = EditablePatientEntry.from(
        patient = patientProfile.patient,
        address = patientProfile.address,
        phoneNumber = patientProfile.phoneNumbers.firstOrNull(),
        dateOfBirthFormatter = dateOfBirthFormatter,
        alternativeId = null
    ).updateAge(enteredAge.toString())
    val expectedPatientToBeSaved = patientProfile.patient.withAgeDetails(PatientAgeDetails.fromAgeOrDate(
        age = Age(enteredAge, currentTime + timeToAdvanceBy),
        date = null
    ))
    whenever(patientRepository.updatePatient(expectedPatientToBeSaved)).thenReturn(Completable.complete())
    whenever(patientRepository.updateAddressForPatient(patientProfile.patientUuid, patientProfile.address)).thenReturn(Completable.complete())

    // when
    utcClock.advanceBy(timeToAdvanceBy)
    testCase.dispatch(SavePatientEffect(
        ongoingEntry = ongoingEntry,
        savedPatient = patientProfile.patient,
        savedAddress = patientProfile.address,
        savedPhoneNumber = patientProfile.phoneNumbers.firstOrNull(),
        saveAlternativeId = null
    ))

    // then
    testCase.assertOutgoingEvents(PatientSaved)
    verifyZeroInteractions(ui)
    verify(patientRepository).updatePatient(expectedPatientToBeSaved)
    verify(patientRepository).updateAddressForPatient(patientProfile.patientUuid, patientProfile.address)
    verifyNoMoreInteractions(patientRepository)
  }
}
