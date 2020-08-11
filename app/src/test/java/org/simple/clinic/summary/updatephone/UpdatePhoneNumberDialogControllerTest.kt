package org.simple.clinic.summary.updatephone

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.registration.phone.LengthBasedNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.Blank
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooLong
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooShort
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.ValidNumber
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.LANDLINE_OR_MOBILE
import org.simple.clinic.util.Just
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class UpdatePhoneNumberDialogControllerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val ui = mock<UpdatePhoneNumberDialogUi>()
  private val repository = mock<PatientRepository>()
  private val validator = LengthBasedNumberValidator(
      minimumRequiredLengthMobile = 6,
      minimumRequiredLengthLandlinesOrMobile = 6,
      maximumAllowedLengthMobile = 12,
      maximumAllowedLengthLandlinesOrMobile = 12
  )

  private val patientUuid = UUID.fromString("0c1c5a00-2416-4a41-8b9e-8059ac18df5d")

  private lateinit var controllerSubscription: Disposable
  private lateinit var testFixture: MobiusTestFixture<UpdatePhoneNumberModel, UpdatePhoneNumberEvent, UpdatePhoneNumberEffect>

  @After
  fun tearDown() {
    controllerSubscription.dispose()
    testFixture.dispose()
  }

  @Test
  fun `when dialog is created, the existing phone number should be pre-filled`() {
    // given
    val phoneNumber = TestData.patientPhoneNumber(
        uuid = UUID.fromString("4ada8db2-71dc-4a3b-8d17-69032cab2155"),
        patientUuid = patientUuid
    )
    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(phoneNumber)))

    // when
    setupController(patientUuid = patientUuid)

    // then
    verify(ui).preFillPhoneNumber(phoneNumber.number)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when save is clicked, the number should be saved if it's valid`() {
    // given
    val newNumber = "1234567890"
    val existingPhoneNumber = TestData.patientPhoneNumber(
        uuid = UUID.fromString("104e74c7-381a-4c07-8728-d6db77086dd3"),
        patientUuid = patientUuid,
        number = "0987654321"
    )

    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(existingPhoneNumber)))
    whenever(repository.updatePhoneNumberForPatient(patientUuid, existingPhoneNumber.updatePhoneNumber(newNumber))).thenReturn(Completable.complete())

    // when
    setupController(patientUuid = patientUuid)
    uiEvents.onNext(UpdatePhoneNumberSaveClicked(newNumber))

    // then
    verify(repository, times(2)).phoneNumber(patientUuid)
    verify(repository).updatePhoneNumberForPatient(patientUuid, existingPhoneNumber.updatePhoneNumber(newNumber))
    verifyNoMoreInteractions(repository)

    verify(ui).preFillPhoneNumber(existingPhoneNumber.number)
    verify(ui).closeDialog()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when save is clicked, the number should not be saved if it's blank and an error should be shown`() {
    // given
    val newNumber = ""
    val existingPhoneNumber = TestData.patientPhoneNumber(
        uuid = UUID.fromString("0e4bf753-009b-4cd6-ae30-aa9935bf2ea6"),
        patientUuid = patientUuid,
        number = "1234567890"
    )

    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(existingPhoneNumber)))
    whenever(repository.updatePhoneNumberForPatient(patientUuid, existingPhoneNumber.updatePhoneNumber(newNumber))).thenReturn(Completable.never())

    // when
    setupController(patientUuid = patientUuid)
    uiEvents.onNext(UpdatePhoneNumberSaveClicked(newNumber))

    // then
    verify(repository, never()).updatePhoneNumberForPatient(patientUuid, existingPhoneNumber.updatePhoneNumber(newNumber))

    verify(ui).preFillPhoneNumber(existingPhoneNumber.number)
    verify(ui).showPhoneNumberTooShortError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when save is clicked, the number should not be saved if it's too short and an error should be shown`() {
    // given
    val newNumber = "123"
    val existingPhoneNumber = TestData.patientPhoneNumber(
        uuid = UUID.fromString("0e4bf753-009b-4cd6-ae30-aa9935bf2ea6"),
        patientUuid = patientUuid,
        number = "1234567890"
    )

    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(existingPhoneNumber)))
    whenever(repository.updatePhoneNumberForPatient(patientUuid, existingPhoneNumber.updatePhoneNumber(newNumber))).thenReturn(Completable.never())

    // when
    setupController(patientUuid = patientUuid)
    uiEvents.onNext(UpdatePhoneNumberSaveClicked(newNumber))

    // then
    verify(repository, never()).updatePhoneNumberForPatient(patientUuid, existingPhoneNumber.updatePhoneNumber(newNumber))

    verify(ui).preFillPhoneNumber(existingPhoneNumber.number)
    verify(ui).showPhoneNumberTooShortError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when save is clicked, the number should not be saved if it's too long and an error should be shown`() {
    // given
    val newNumber = "1234567890123"
    val existingPhoneNumber = TestData.patientPhoneNumber(
        uuid = UUID.fromString("0e4bf753-009b-4cd6-ae30-aa9935bf2ea6"),
        patientUuid = patientUuid,
        number = "1234567890"
    )

    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(existingPhoneNumber)))
    whenever(repository.updatePhoneNumberForPatient(patientUuid, existingPhoneNumber.updatePhoneNumber(newNumber))).thenReturn(Completable.never())

    // when
    setupController(patientUuid = patientUuid)
    uiEvents.onNext(UpdatePhoneNumberSaveClicked(newNumber))

    // then
    verify(repository, never()).updatePhoneNumberForPatient(patientUuid, existingPhoneNumber.updatePhoneNumber(newNumber))

    verify(ui).preFillPhoneNumber(existingPhoneNumber.number)
    verify(ui).showPhoneNumberTooLongError()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when cancel is clicked then the existing number should be saved again`() {
    // given
    val existingPhoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid)
    whenever(repository.phoneNumber(patientUuid)).thenReturn(Observable.just(Just(existingPhoneNumber)))
    whenever(repository.updatePhoneNumberForPatient(patientUuid, existingPhoneNumber)).thenReturn(Completable.complete())

    // when
    setupController(patientUuid = patientUuid)
    uiEvents.onNext(UpdatePhoneNumberCancelClicked)

    // then
    verify(repository, times(2)).phoneNumber(patientUuid)
    verify(repository).updatePhoneNumberForPatient(patientUuid, existingPhoneNumber)
    verifyNoMoreInteractions(repository)
  }

  private fun setupController(patientUuid: PatientUuid) {
    val controller = UpdatePhoneNumberDialogController(
        patientUuid = patientUuid,
        repository = repository,
        validator = validator
    )

    controllerSubscription = uiEvents
        .compose(controller)
        .subscribe { uiChange -> uiChange(ui) }

    uiEvents.onNext(ScreenCreated())

    val effectHandler = UpdatePhoneNumberEffectHandler(
        patientRepository = repository,
        validator = validator,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        uiActions = ui
    )

    val uiRenderer = UpdatePhoneNumberUiRenderer(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = UpdatePhoneNumberModel.create(patientUuid),
        init = UpdatePhoneNumberInit(),
        update = UpdatePhoneNumberUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render
    )

    testFixture.start()
  }

  fun PatientPhoneNumber.updatePhoneNumber(phoneNumber: String): PatientPhoneNumber {
    return copy(number = phoneNumber)
  }
}
