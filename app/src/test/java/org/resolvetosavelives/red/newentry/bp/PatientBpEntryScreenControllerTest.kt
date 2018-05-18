package org.resolvetosavelives.red.newentry.bp

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.newentry.search.Gender
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.UiEvent

class PatientBpEntryScreenControllerTest {

  private val screen: PatientBpEntryScreen = mock()
  private val repository: PatientRepository = mock()
  private val dummyBpMeasurements = OngoingPatientEntry.BloodPressureMeasurement(142, 95)
  private val dummyPersonalDetails = OngoingPatientEntry.PersonalDetails(
      fullName = "Ashok Kumar",
      dateOfBirth = "01/01/1900",
      ageWhenCreated = 118,
      gender = Gender.TRANS)

  private lateinit var controller: PatientBpEntryScreenController
  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()

  @Before
  fun setUp() {
    controller = PatientBpEntryScreenController(repository)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts then the keyboard should be shown on the systolic field`() {
    uiEvents.onNext(ScreenCreated())

    verify(screen).showKeyboardOnSystolicField()
  }

  @Test
  fun `when screen starts and existing blood pressure measurements are present then they should be pre-filled`() {
    val dummyEntry = OngoingPatientEntry(personalDetails = dummyPersonalDetails, bloodPressureMeasurements = dummyBpMeasurements)
    whenever(repository.ongoingEntry()).thenReturn(Single.just(dummyEntry))

    uiEvents.onNext(ScreenCreated())

    verify(screen).preFill(dummyPersonalDetails.fullName)
    verify(screen).preFill(dummyBpMeasurements)
  }

  @Test
  fun `when screen starts and existing patient name and measurements are not present then they should not be pre-filled`() {
    whenever(repository.ongoingEntry()).thenReturn(Single.just(OngoingPatientEntry()))

    uiEvents.onNext(ScreenCreated())

    verify(screen, never()).preFill(any<String>())
    verify(screen, never()).preFill(any<OngoingPatientEntry.BloodPressureMeasurement>())
  }

  @Test
  fun `when proceed is clicked then measurements should be saved and the next screen should be opened`() {
    val ongoingEntry = OngoingPatientEntry(personalDetails = dummyPersonalDetails, bloodPressureMeasurements = dummyBpMeasurements)
    whenever(repository.ongoingEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(repository.save(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientBpSystolicTextChanged(dummyBpMeasurements.systolic))
    uiEvents.onNext(PatientBpDiastolicTextChanged(dummyBpMeasurements.diastolic))
    uiEvents.onNext(PatientBpEntryProceedClicked())

    argumentCaptor<OngoingPatientEntry>().apply {
      verify(repository).save(capture())
      assert(firstValue.bloodPressureMeasurements == dummyBpMeasurements)
    }
    verify(screen).openDrugSelectionScreen()
  }
}
