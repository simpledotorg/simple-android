package org.simple.clinic.newentry

import com.google.common.truth.Truth.assertThat
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.simple.clinic.analytics.MockAnalyticsReporter
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.newentry.country.BangladeshInputFieldsProvider
import org.simple.clinic.newentry.country.InputFieldsFactory
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Gender.Female
import org.simple.clinic.patient.Gender.Male
import org.simple.clinic.patient.Gender.Transgender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.OngoingNewPatientEntry.Address
import org.simple.clinic.patient.OngoingNewPatientEntry.PersonalDetails
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.ageanddateofbirth.DateOfBirthAndAgeVisibility
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.mobius.migration.MobiusTestFixture
import org.simple.clinic.TestData
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.TestUserClock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale.ENGLISH
import java.util.Optional
import java.util.UUID

@RunWith(JUnitParamsRunner::class)
class PatientEntryScreenLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val ui = mock<PatientEntryUi>()
  private val uiActions = mock<PatientEntryUiActions>()
  private val patientRepository = mock<PatientRepository>()
  private val facilityRepository = mock<FacilityRepository>()
  private val userClock: UserClock = TestUserClock(LocalDate.parse("2018-01-01"))
  private val dobValidator = UserInputDateValidator(userClock, DateTimeFormatter.ofPattern("dd/MM/yyyy", ENGLISH))
  private val numberValidator = PhoneNumberValidator(minimumRequiredLength = 6)
  private val ageValidator = UserInputAgeValidator(userClock, DateTimeFormatter.ofPattern("dd/MM/yyyy", ENGLISH))

  private val uiEvents = PublishSubject.create<PatientEntryEvent>()
  private lateinit var fixture: MobiusTestFixture<PatientEntryModel, PatientEntryEvent, PatientEntryEffect>
  private val reporter = MockAnalyticsReporter()

  private val inputFieldsFactory = InputFieldsFactory(BangladeshInputFieldsProvider())

  private lateinit var errorConsumer: (Throwable) -> Unit

  @Before
  fun setUp() {
    whenever(facilityRepository.currentFacility())
        .doReturn(Observable.just(TestData.facility(
            uuid = UUID.fromString("563c12be-fe24-4e19-a02a-dd87c18ff767"),
            villageOrColony = "village",
            district = "district",
            state = "state",
            streetAddress = "street"
        )))

    val viewEffectHandler = PatientEntryViewEffectHandler(uiActions)
    val effectHandler = PatientEntryEffectHandler(
        facilityRepository = facilityRepository,
        patientRepository = patientRepository,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        inputFieldsFactory = inputFieldsFactory,
        viewEffectsConsumer = viewEffectHandler::handle
    )

    fixture = MobiusTestFixture(
        uiEvents,
        PatientEntryModel.DEFAULT,
        PatientEntryInit(),
        PatientEntryUpdate(numberValidator, dobValidator, ageValidator),
        effectHandler.build(),
        PatientEntryUiRenderer(ui)::render
    )

    Analytics.addReporter(reporter)
  }

  @After
  fun tearDown() {
    Analytics.removeReporter(reporter)
    reporter.clear()
  }

  @Test
  fun `when screen is created then existing data should be pre-filled`() {
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry())

    screenCreated()

    verify(uiActions).prefillFields(OngoingNewPatientEntry(
        address = Address(
            colonyOrVillage = "",
            district = "district",
            state = "state",
            streetAddress = "",
            zone = ""
        )
    ))
  }

  @Test
  fun `when screen is created with an already present address, the already present address must be used for prefilling`() {
    val address = Address(
        colonyOrVillage = "colony 1",
        district = "district 2",
        state = "state 3",
        streetAddress = "streetAddress",
        zone = "zone"
    )
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry(address = address))

    screenCreated()

    verify(uiActions).prefillFields(OngoingNewPatientEntry(address = address))
  }

  @Test
  fun `when save button is clicked then a patient record should be created from the form input`() {
    val colonyOrVillages = listOf("colony1", "colony2", "colony3", "colony4")
    whenever(patientRepository.allColoniesOrVillagesInPatientAddress()).thenReturn(colonyOrVillages)

    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry())
    screenCreated()

    with(uiEvents) {
      onNext(FullNameChanged("Ashok"))
      onNext(PhoneNumberChanged("1234567890"))
      onNext(DateOfBirthChanged("12/04/1993"))
      onNext(AgeChanged(""))
      onNext(GenderChanged(Optional.of(Transgender)))
      onNext(ColonyOrVillageChanged("colony"))
      onNext(DistrictChanged("district"))
      onNext(StateChanged("state"))
      onNext(StreetAddressChanged("streetAddress"))
      onNext(ZoneChanged("zone"))
      onNext(SaveClicked)
    }

    verify(patientRepository).ongoingEntry()
    verify(patientRepository).saveOngoingEntry(OngoingNewPatientEntry(
        personalDetails = PersonalDetails("Ashok", "12/04/1993", age = null, gender = Transgender),
        address = Address(
            colonyOrVillage = "colony",
            district = "district",
            state = "state",
            streetAddress = "streetAddress",
            zone = "zone"
        ),
        phoneNumber = OngoingNewPatientEntry.PhoneNumber("1234567890")
    ))
    verify(patientRepository).allColoniesOrVillagesInPatientAddress()
    verifyNoMoreInteractions(patientRepository)
    verify(uiActions).openMedicalHistoryEntryScreen()
  }

  @Test
  fun `when bangladesh national id is entered and save is clicked then patient should get registered`() {
    val bangladeshNationalId = Identifier("123456789012", BangladeshNationalId)

    val colonyOrVillages = listOf("colony1", "colony2", "colony3", "colony4")
    whenever(patientRepository.allColoniesOrVillagesInPatientAddress()).thenReturn(colonyOrVillages)

    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry())
    screenCreated()

    with(uiEvents) {
      onNext(FullNameChanged("Ashok"))
      onNext(PhoneNumberChanged("1234567890"))
      onNext(AlternativeIdChanged(bangladeshNationalId))
      onNext(DateOfBirthChanged("12/04/1993"))
      onNext(AgeChanged(""))
      onNext(GenderChanged(Optional.of(Transgender)))
      onNext(ColonyOrVillageChanged("colony"))
      onNext(DistrictChanged("district"))
      onNext(StateChanged("state"))
      onNext(StreetAddressChanged("streetAddress"))
      onNext(ZoneChanged("zone"))
      onNext(SaveClicked)
    }

    verify(patientRepository).ongoingEntry()
    verify(patientRepository).saveOngoingEntry(OngoingNewPatientEntry(
        personalDetails = PersonalDetails("Ashok", "12/04/1993", age = null, gender = Transgender),
        address = Address(
            colonyOrVillage = "colony",
            district = "district",
            state = "state",
            streetAddress = "streetAddress",
            zone = "zone"
        ),
        phoneNumber = OngoingNewPatientEntry.PhoneNumber("1234567890"),
        alternateId = bangladeshNationalId
    ))
    verify(patientRepository).allColoniesOrVillagesInPatientAddress()
    verifyNoMoreInteractions(patientRepository)
    verify(uiActions).openMedicalHistoryEntryScreen()
  }

  @Test
  fun `date-of-birth and age fields should only be visible while one of them is empty`() {
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry())
    screenCreated()

    with(uiEvents) {
      onNext(AgeChanged(""))
      onNext(DateOfBirthChanged(""))
    }

    verify(ui).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.BOTH_VISIBLE)

    uiEvents.onNext(DateOfBirthChanged("1"))
    verify(ui).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.DATE_OF_BIRTH_VISIBLE)

    with(uiEvents) {
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("1"))
    }
    verify(ui).setDateOfBirthAndAgeVisibility(DateOfBirthAndAgeVisibility.AGE_VISIBLE)
  }

  // TODO(rj): 2019-10-04 This test keeps passing no matter what, I verified if the exception is being thrown
  //                      by setting a breakpoint. Revisit this test again, this also has a related
  //                      pivotal story - https://www.pivotaltracker.com/story/show/168946268
  @Test
  fun `when both date-of-birth and age fields have text then an assertion error should be thrown`() {
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry())
    screenCreated()
    errorConsumer = { assertThat(it).isInstanceOf(AssertionError::class.java) }

    with(uiEvents) {
      onNext(DateOfBirthChanged("1"))
      onNext(AgeChanged("1"))
    }
  }

  @Test
  fun `while date-of-birth has focus or has some input then date format should be shown in the label`() {
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry())
    screenCreated()

    with(uiEvents) {
      onNext(DateOfBirthChanged(""))
      onNext(DateOfBirthFocusChanged(hasFocus = false))
      onNext(DateOfBirthFocusChanged(hasFocus = true))
      onNext(DateOfBirthChanged("1"))
      onNext(DateOfBirthFocusChanged(hasFocus = false))
    }

    verify(uiActions).setShowDatePatternInDateOfBirthLabel(false)
    verify(uiActions).setShowDatePatternInDateOfBirthLabel(true)
  }

  @Test
  fun `when save is clicked then user input should be validated`() {
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry())
    screenCreated()

    with(uiEvents) {
      onNext(FullNameChanged(""))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged(""))
      onNext(GenderChanged(Optional.empty()))
      onNext(ColonyOrVillageChanged(""))
      onNext(DistrictChanged(""))
      onNext(StateChanged(""))
      onNext(SaveClicked)
    }

    with(uiEvents) {
      onNext(DateOfBirthChanged("33/33/3333"))
      onNext(SaveClicked)
    }

    with(uiEvents) {
      onNext(AgeChanged(" "))
      onNext(DateOfBirthChanged(""))
      onNext(SaveClicked)
    }

    with(uiEvents) {
      onNext(DateOfBirthChanged("16/07/2018"))
      onNext(SaveClicked)
    }

    with(uiEvents) {
      onNext(PhoneNumberChanged("1234"))
      onNext(SaveClicked)
    }

    with(uiEvents) {
      onNext(PhoneNumberChanged("1234567890987654"))
      onNext(SaveClicked)
    }

    // These get invoked every time the phone number changes
    verify(uiActions, times(3)).showLengthTooShortPhoneNumberError(false, 0)

    verify(uiActions, atLeastOnce()).showEmptyFullNameError(true)
    verify(uiActions, atLeastOnce()).showEmptyDateOfBirthAndAgeError(true)
    verify(uiActions, atLeastOnce()).showInvalidDateOfBirthError(true)
    verify(uiActions, atLeastOnce()).showMissingGenderError(true)
    verify(uiActions, atLeastOnce()).showEmptyColonyOrVillageError(true)
    verify(uiActions, atLeastOnce()).showEmptyDistrictError(true)
    verify(uiActions, atLeastOnce()).showEmptyStateError(true)
    verify(uiActions, atLeastOnce()).showLengthTooShortPhoneNumberError(true, 6)
  }

  @Test
  fun `when input validation fails, the errors must be sent to analytics`() {
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry())
    screenCreated()

    with(uiEvents) {
      onNext(FullNameChanged(""))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged(""))
      onNext(GenderChanged(Optional.empty()))
      onNext(ColonyOrVillageChanged(""))
      onNext(DistrictChanged(""))
      onNext(StateChanged(""))
      onNext(SaveClicked)
    }

    with(uiEvents) {
      onNext(DateOfBirthChanged("33/33/3333"))
      onNext(SaveClicked)
    }

    with(uiEvents) {
      onNext(AgeChanged(" "))
      onNext(DateOfBirthChanged(""))
      onNext(SaveClicked)
    }

    with(uiEvents) {
      onNext(DateOfBirthChanged("16/07/2018"))
      onNext(SaveClicked)
    }

    val validationErrors = reporter.receivedEvents
        .filter { it.name == "InputValidationError" }

    assertThat(validationErrors).isNotEmpty()
  }

  @Test
  fun `validation errors should be cleared on every input change`() {
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry())
    screenCreated()

    with(uiEvents) {
      onNext(FullNameChanged("Ashok"))
      onNext(PhoneNumberChanged("1234567890"))
      onNext(DateOfBirthChanged("12/04/1993"))
      onNext(GenderChanged(Optional.of(Transgender)))
      onNext(ColonyOrVillageChanged("colony"))
      onNext(DistrictChanged("district"))
      onNext(StateChanged("state"))

      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("20"))
      onNext(ColonyOrVillageChanged(""))
    }

    verify(uiActions).showEmptyFullNameError(false)
    verify(uiActions, atLeastOnce()).showEmptyDateOfBirthAndAgeError(false)
    verify(uiActions, atLeastOnce()).showInvalidDateOfBirthError(false)
    verify(uiActions, atLeastOnce()).showDateOfBirthIsInFutureError(false)
    verify(uiActions).showMissingGenderError(false)
    verify(uiActions, atLeastOnce()).showEmptyColonyOrVillageError(false)
    verify(uiActions).showEmptyDistrictError(false)
    verify(uiActions).showEmptyStateError(false)
  }

  // TODO: Write these similarly structured regression tests in a smarter way.

  @Test
  fun `regression test for validations 1`() {
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry())
    screenCreated()

    with(uiEvents) {
      onNext(FullNameChanged("Ashok Kumar"))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("20"))
      onNext(GenderChanged(Optional.of(Male)))
      onNext(ColonyOrVillageChanged(""))
      onNext(DistrictChanged("District"))
      onNext(StateChanged("State"))

      onNext(SaveClicked)
    }

    verify(uiActions, never()).openMedicalHistoryEntryScreen()
    verify(patientRepository, never()).saveOngoingEntry(any())
  }

  @Test
  fun `regression test for validations 2`() {
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry())
    screenCreated()

    with(uiEvents) {
      onNext(FullNameChanged("Ashok Kumar"))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("20"))
      onNext(GenderChanged(Optional.of(Male)))
      onNext(ColonyOrVillageChanged(""))
      onNext(DistrictChanged("District"))
      onNext(StateChanged("State"))

      onNext(SaveClicked)
    }

    verify(uiActions, never()).openMedicalHistoryEntryScreen()
    verify(patientRepository, never()).saveOngoingEntry(any())
  }

  @Test
  fun `regression test for validations 3`() {
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry())
    screenCreated()

    with(uiEvents) {
      onNext(FullNameChanged("Ashok Kumar"))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("20"))
      onNext(GenderChanged(Optional.empty()))
      onNext(ColonyOrVillageChanged(""))
      onNext(DistrictChanged("District"))
      onNext(StateChanged("State"))

      onNext(SaveClicked)
    }

    verify(uiActions, never()).openMedicalHistoryEntryScreen()
    verify(patientRepository, never()).saveOngoingEntry(any())
  }

  // TODO (vs) 27/10/20: Tighten assertions in this test`
  @Test
  fun `regression test for validations 4`() {
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry())
    screenCreated()

    with(uiEvents) {
      onNext(FullNameChanged("Ashok Kumar"))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("20"))
      onNext(GenderChanged(Optional.of(Female)))
      onNext(ColonyOrVillageChanged("Colony"))
      onNext(DistrictChanged("District"))
      onNext(StateChanged("State"))

      onNext(SaveClicked)
    }

    verify(uiActions).openMedicalHistoryEntryScreen()
    verify(patientRepository).saveOngoingEntry(any())
  }

  @Test
  @Parameters(method = "params for gender values")
  fun `when gender is selected for the first time then the form should be scrolled to bottom`(gender: Gender) {
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry())
    screenCreated()

    with(uiEvents) {
      onNext(GenderChanged(Optional.empty()))
      onNext(GenderChanged(Optional.of(gender)))
      onNext(GenderChanged(Optional.of(gender)))
    }

    verify(uiActions, times(1)).scrollFormOnGenderSelection()
  }

  @Suppress("Unused")
  private fun `params for gender values`(): List<Gender> {
    return listOf(Male, Female, Transgender)
  }

  @Test
  fun `when validation errors are shown then the form should be scrolled to the first field with error`() {
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry())
    screenCreated()

    with(uiEvents) {
      onNext(FullNameChanged("Ashok Kumar"))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("20"))
      onNext(GenderChanged(Optional.of(Transgender)))
      onNext(ColonyOrVillageChanged(""))
      onNext(DistrictChanged(""))
      onNext(StateChanged(""))

      onNext(SaveClicked)
    }

    // This is order dependent because finding the first field
    // with error is only possible once the errors are set.
    val inOrder = inOrder(uiActions)
    inOrder.verify(uiActions).showEmptyDistrictError(true)
    inOrder.verify(uiActions).scrollToFirstFieldWithError()
  }

  @Test
  fun `when the ongoing entry has an identifier it must be retained when accepting input`() {
    val identifier = Identifier(value = "id", type = BpPassport)
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry(identifier = identifier))
    screenCreated()

    with(uiEvents) {
      onNext(FullNameChanged("Ashok Kumar"))
      onNext(PhoneNumberChanged(""))
      onNext(DateOfBirthChanged(""))
      onNext(AgeChanged("20"))
      onNext(GenderChanged(Optional.of(Female)))
      onNext(ColonyOrVillageChanged("Colony"))
      onNext(DistrictChanged("District"))
      onNext(StateChanged("State"))
      onNext(StreetAddressChanged("streetAddress"))
      onNext(ZoneChanged("zone"))
      onNext(SaveClicked)
    }

    val expectedSavedEntry = OngoingNewPatientEntry(
        personalDetails = PersonalDetails(
            fullName = "Ashok Kumar",
            dateOfBirth = null,
            age = "20",
            gender = Female
        ),
        address = Address(
            colonyOrVillage = "Colony",
            district = "District",
            state = "State",
            streetAddress = "streetAddress",
            zone = "zone"
        ),
        phoneNumber = null,
        identifier = identifier
    )
    verify(patientRepository).saveOngoingEntry(expectedSavedEntry)
  }

  @Test
  fun `when the ongoing patient entry has an identifier, the identifier section must be shown`() {
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry(identifier = Identifier("id", BpPassport)))

    screenCreated()

    verify(ui).showIdentifierSection()
  }

  @Test
  fun `when the ongoing patient entry does not have an identifier, the identifier section must be hidden`() {
    whenever(patientRepository.ongoingEntry()).doReturn(OngoingNewPatientEntry(identifier = null))

    screenCreated()

    verify(ui).hideIdentifierSection()
  }

  private fun screenCreated() {
    fixture.start()
  }
}
