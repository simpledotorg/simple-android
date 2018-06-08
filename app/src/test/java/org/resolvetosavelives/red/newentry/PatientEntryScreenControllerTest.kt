package org.resolvetosavelives.red.newentry

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.facility.Facility
import org.resolvetosavelives.red.facility.FacilityRepository
import org.resolvetosavelives.red.patient.Gender
import org.resolvetosavelives.red.patient.OngoingPatientEntry
import org.resolvetosavelives.red.patient.PatientRepository
import org.resolvetosavelives.red.util.Just
import org.resolvetosavelives.red.util.None
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.UiEvent

class PatientEntryScreenControllerTest {

  private val screen = mock<PatientEntryScreen>()
  private val patientRepository = mock<PatientRepository>()
  private val dateOfBirthValidator = mock<DateOfBirthFormatValidator>()
  private val facilityRepository = mock<FacilityRepository>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val controller = PatientEntryScreenController(patientRepository, facilityRepository, dateOfBirthValidator)

  @Before
  fun setUp() {
    whenever(facilityRepository.currentFacility()).thenReturn(Observable.just(Facility(district = "district", state = "state")))

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen is created then existing data should be pre-filled`() {
    whenever(patientRepository.ongoingEntry()).thenReturn(Single.just(OngoingPatientEntry()))

    uiEvents.onNext(ScreenCreated())

    verify(screen).preFillFields(OngoingPatientEntry(
        address = OngoingPatientEntry.Address(
            colonyOrVillage = "",
            district = "district",
            state = "state")))
  }

  @Test
  fun `save button should remain disabled until the form has sufficient input`() {
    whenever(dateOfBirthValidator.validate("")).thenReturn(DateOfBirthFormatValidator.Result.VALID)

    // Default, empty values when the screen starts.
    uiEvents.onNext(PatientFullNameTextChanged(""))
    uiEvents.onNext(PatientNoPhoneNumberToggled(noneSelected = false))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged(""))
    uiEvents.onNext(PatientGenderChanged(None))
    uiEvents.onNext(PatientColonyOrVillageTextChanged(""))
    uiEvents.onNext(PatientDistrictTextChanged(""))
    uiEvents.onNext(PatientStateTextChanged(""))

    // Valid values entered later.
    uiEvents.onNext(PatientFullNameTextChanged("Ashok"))
    uiEvents.onNext(PatientNoPhoneNumberToggled(noneSelected = true))
    uiEvents.onNext(PatientPhoneNumberTextChanged(""))
    uiEvents.onNext(PatientDateOfBirthTextChanged(""))
    uiEvents.onNext(PatientAgeTextChanged("125"))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.TRANSGENDER)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged("colony-or-village"))
    uiEvents.onNext(PatientDistrictTextChanged("district"))
    uiEvents.onNext(PatientStateTextChanged("state"))

    verify(screen, times(1)).setSaveButtonEnabled(false)
    verify(screen, times(1)).setSaveButtonEnabled(true)
  }

  @Test
  fun `when save button is clicked then a patient record should be created from the form input`() {
    whenever(patientRepository.saveOngoingEntry(any())).thenReturn(Completable.complete())
    whenever(dateOfBirthValidator.validate(any())).thenReturn(DateOfBirthFormatValidator.Result.VALID)
    whenever(patientRepository.saveOngoingEntryAsPatient()).thenReturn(Completable.complete())

    uiEvents.onNext(PatientFullNameTextChanged("Ashok"))
    uiEvents.onNext(PatientNoPhoneNumberToggled(noneSelected = false))
    uiEvents.onNext(PatientPhoneNumberTextChanged("1234567890"))
    uiEvents.onNext(PatientDateOfBirthTextChanged("12-04-1993"))
    uiEvents.onNext(PatientAgeTextChanged(""))
    uiEvents.onNext(PatientGenderChanged(Just(Gender.TRANSGENDER)))
    uiEvents.onNext(PatientColonyOrVillageTextChanged("colony-or-village"))
    uiEvents.onNext(PatientDistrictTextChanged("district"))
    uiEvents.onNext(PatientStateTextChanged("state"))
    uiEvents.onNext(PatientEntrySaveClicked())

    verify(patientRepository).saveOngoingEntry(OngoingPatientEntry(
        personalDetails = OngoingPatientEntry.PersonalDetails("Ashok", "12-04-1993", age = null, gender = Gender.TRANSGENDER),
        address = OngoingPatientEntry.Address("colony-or-village", "district", "state"),
        phoneNumber = OngoingPatientEntry.PhoneNumber("1234567890")
    ))
    verify(patientRepository).saveOngoingEntryAsPatient()
    verify(screen).openSummaryScreenForBpEntry()
  }
}
