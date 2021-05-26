package org.simple.clinic.summary.addphone

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.spotify.mobius.Init
import io.reactivex.Completable
import io.reactivex.rxkotlin.ofType
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.mobius.first
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.PhoneNumberDetails
import org.simple.clinic.registration.phone.LengthBasedNumberValidator
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import org.simple.clinic.uuid.FakeUuidGenerator
import org.simple.clinic.widgets.UiEvent
import org.simple.mobius.migration.MobiusTestFixture
import java.util.UUID

class AddPhoneNumberDialogLogicTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private val uiEvents = PublishSubject.create<UiEvent>()
  private val ui = mock<AddPhoneNumberUi>()
  private val uiActions = mock<UiActions>()
  private val repository = mock<PatientRepository>()

  private val validator = LengthBasedNumberValidator(
      minimumRequiredLengthMobile = 6,
      maximumAllowedLengthMobile = 12,
      minimumRequiredLengthLandlinesOrMobile = 6,
      maximumAllowedLengthLandlinesOrMobile = 12
  )

  private val patientUuid = UUID.fromString("b2d1e529-8ee9-43f3-bacc-72fe1e73daa6")
  private val generatedPhoneUuid = UUID.fromString("f94bd99b-b182-4138-8e77-d91908b7ada5")

  private lateinit var testFixture: MobiusTestFixture<AddPhoneNumberModel, AddPhoneNumberEvent, AddPhoneNumberEffect>

  @After
  fun tearDown() {
    testFixture.dispose()
  }

  @Test
  fun `when save is clicked, the number should be saved if it's valid`() {
    // given
    val newNumber = "1234567890"
    val numberDetails = PhoneNumberDetails.mobile(newNumber)

    whenever(repository.createPhoneNumberForPatient(
        uuid = generatedPhoneUuid,
        patientUuid = patientUuid,
        numberDetails = numberDetails,
        active = true
    )).thenReturn(Completable.complete())

    // when
    setupController()
    uiEvents.onNext(AddPhoneNumberSaveClicked(newNumber))

    // then
    verify(repository).createPhoneNumberForPatient(
        uuid = generatedPhoneUuid,
        patientUuid = patientUuid,
        numberDetails = numberDetails,
        active = true
    )
    verifyNoMoreInteractions(repository)
    verify(ui).clearPhoneNumberError()
    verifyNoMoreInteractions(ui)
    verify(uiActions).closeDialog()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when save is clicked, the number should not be saved if it's blank`() {
    // given
    val newNumber = ""

    // when
    setupController()
    uiEvents.onNext(AddPhoneNumberSaveClicked(newNumber))

    // then
    verify(repository, never()).createPhoneNumberForPatient(any(), any(), any(), any())
    verifyNoMoreInteractions(repository)

    verify(ui).showPhoneNumberBlank()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when save is clicked, the number should not be saved if it's too short`() {
    // given
    val newNumber = "123"

    // when
    setupController()
    uiEvents.onNext(AddPhoneNumberSaveClicked(newNumber))

    // then
    verify(repository, never()).createPhoneNumberForPatient(any(), any(), any(), any())
    verifyNoMoreInteractions(repository)

    verify(ui).showPhoneNumberTooShortError(6)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when save is clicked, the number should not be saved if it's too long`() {
    // given
    val newNumber = "1234567890123"

    // when
    setupController()
    uiEvents.onNext(AddPhoneNumberSaveClicked(newNumber))

    // then
    verify(repository, never()).createPhoneNumberForPatient(any(), any(), any(), any())
    verifyNoMoreInteractions(repository)

    verify(ui).showPhoneNumberTooLongError(12)
    verifyNoMoreInteractions(ui)
  }

  private fun setupController() {
    val uuidGenerator = FakeUuidGenerator.fixed(generatedPhoneUuid)

    val effectHandler = AddPhoneNumberEffectHandler(
        repository = repository,
        uuidGenerator = uuidGenerator,
        validator = validator,
        schedulersProvider = TestSchedulersProvider.trampoline(),
        uiActions = uiActions
    )
    val uiRenderer = AddPhoneNumberUiRender(ui)

    testFixture = MobiusTestFixture(
        events = uiEvents.ofType(),
        defaultModel = AddPhoneNumberModel.create(patientUuid),
        update = AddPhoneNumberUpdate(),
        effectHandler = effectHandler.build(),
        modelUpdateListener = uiRenderer::render,
        init = Init { first(it) }
    )

    testFixture.start()
  }
}
