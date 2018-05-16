package org.resolvetosavelives.red.newentry.personal

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.widgets.UiEvent

class PatientPersonalDetailsEntryScreenControllerTest {

  private val screen: PatientPersonalDetailsEntryScreen = mock()
  private lateinit var repository: PatientRepository

  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()
  private lateinit var controller: PatientPersonalDetailsEntryScreenController

  @Before
  fun setUp() {
    repository = mock {
      on { ongoingEntry() }.doReturn(Single.just(OngoingPatientEntry(mobileNumber = "9999999999")))
      on { save(any()) }.doReturn(Completable.complete())
    }

    controller = PatientPersonalDetailsEntryScreenController(repository)

    uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts then the keyboard should be shown on full name field`() {
    verify(screen).showKeyboardOnFullnameField()
  }

  @Test
  fun `when proceed event is clicked then personal details should be saved and the next screen should be opened`() {
    val patientName = "Ashok Kumar"
    uiEvents.onNext(PatientFullNameTextChanged(patientName))
    uiEvents.onNext(PatientPersonalDetailsProceedClicked())

    argumentCaptor<OngoingPatientEntry>().apply {
      verify(repository).save(capture())
      assertEquals(patientName, firstValue.personalDetails!!.fullName)
    }

    verify(screen).openAddressEntryScreen()
  }
}
