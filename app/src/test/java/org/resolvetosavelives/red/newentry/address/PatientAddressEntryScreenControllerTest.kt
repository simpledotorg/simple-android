package org.resolvetosavelives.red.newentry.address

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
import org.resolvetosavelives.red.newentry.search.OngoingPatientEntry
import org.resolvetosavelives.red.newentry.search.PatientRepository
import org.resolvetosavelives.red.widgets.ScreenCreated
import org.resolvetosavelives.red.widgets.UiEvent

class PatientAddressEntryScreenControllerTest {

  private val screen: PatientAddressEntryScreen = mock()
  private val repository: PatientRepository = mock()
  private val uiEvents: PublishSubject<UiEvent> = PublishSubject.create()
  private val dummyAddress = OngoingPatientEntry.Address("Rajinder Nagar", "Hoshiarpur", "Punjab")

  private lateinit var controller: PatientAddressEntryScreenController

  @Before
  fun setUp() {
    controller = PatientAddressEntryScreenController(repository)

    uiEvents.compose(controller).subscribe { uiChange -> uiChange(screen) }
  }

  @Test
  fun `when screen starts then the keyboard should be shown on colony field`() {
    uiEvents.onNext(ScreenCreated())
    verify(screen).showKeyboardOnColonyField()
  }

  @Test
  fun `when screen starts and existing address are present then it should be pre-filled`() {
    val ongoingEntry = OngoingPatientEntry(address = dummyAddress)
    whenever(repository.ongoingEntry()).thenReturn(Single.just(ongoingEntry))

    uiEvents.onNext(ScreenCreated())

    verify(screen).preFill(ongoingEntry.address!!)
  }

  @Test
  fun `when screen starts and existing address are not present then it should not be pre-filled`() {
    val ongoingEntry = OngoingPatientEntry(address = null)
    whenever(repository.ongoingEntry()).thenReturn(Single.just(ongoingEntry))

    uiEvents.onNext(ScreenCreated())

    verify(screen, never()).preFill(any())
  }

  @Test
  fun `when proceed is clicked then the address should be saved and the next screen should be opened`() {
    val ongoingEntry = OngoingPatientEntry(address = dummyAddress)
    whenever(repository.ongoingEntry()).thenReturn(Single.just(ongoingEntry))
    whenever(repository.saveOngoingEntry(any())).thenReturn(Completable.complete())

    uiEvents.onNext(PatientAddressColonyOrVillageTextChanged(dummyAddress.colonyOrVillage))
    uiEvents.onNext(PatientAddressDistrictTextChanged(dummyAddress.district))
    uiEvents.onNext(PatientAddressStateTextChanged(dummyAddress.state))
    uiEvents.onNext(PatientAddressEntryProceedClicked())

    argumentCaptor<OngoingPatientEntry>().apply {
      verify(repository).saveOngoingEntry(capture())
      assert(firstValue.address == dummyAddress)
    }
    verify(screen).openPatientPhoneEntryScreen()
  }
}
