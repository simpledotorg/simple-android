package org.resolvetosavelives.red.newentry

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.tspoon.traceur.Traceur
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.patient.Gender
import org.resolvetosavelives.red.patient.OngoingPatientEntry
import org.resolvetosavelives.red.patient.PatientRepository
import org.resolvetosavelives.red.util.Just
import org.resolvetosavelives.red.util.None
import org.resolvetosavelives.red.widgets.UiEvent

class PatientEntryScreenControllerTest {

  private val screen = mock<PatientEntryScreen>()
  private val repository = mock<PatientRepository>()
  private val dateOfBirthValidator = mock<DateOfBirthFormatValidator>()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val controller = PatientEntryScreenController(repository, dateOfBirthValidator)

  @Before
  fun setUp() {
    Traceur.enableLogging()

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
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
    whenever(repository.saveOngoingEntry(any())).thenReturn(Completable.complete())
    whenever(dateOfBirthValidator.validate(any())).thenReturn(DateOfBirthFormatValidator.Result.VALID)
    whenever(repository.saveOngoingEntryAsPatient()).thenReturn(Completable.complete())

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

    verify(repository).saveOngoingEntry(OngoingPatientEntry(
        personalDetails = OngoingPatientEntry.PersonalDetails("Ashok", "12-04-1993", age = null, gender = Gender.TRANSGENDER),
        address = OngoingPatientEntry.Address("colony-or-village", "district", "state"),
        phoneNumber = OngoingPatientEntry.PhoneNumber("1234567890")
    ))
    verify(repository).saveOngoingEntryAsPatient()
    verify(screen).openSummaryScreenForBpEntry()
  }
}
