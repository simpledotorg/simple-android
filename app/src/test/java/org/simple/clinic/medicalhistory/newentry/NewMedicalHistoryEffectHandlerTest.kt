package org.simple.clinic.medicalhistory.newentry

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.verifyNoInteractions
import dagger.Lazy
import org.junit.After
import org.junit.Test
import org.simple.sharedTestCode.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.sync.DataSync
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.sharedTestCode.uuid.FakeUuidGenerator
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class NewMedicalHistoryEffectHandlerTest {

  private val uiActions = mock<NewMedicalHistoryUiActions>()
  private val viewEffectHandler = NewMedicalHistoryViewEffectHandler(uiActions)
  private val dataSync = mock<DataSync>()
  private val user = TestData.loggedInUser(uuid = UUID.fromString("c70eb25b-c665-4f9d-a889-bf5504ec8af0"))
  private val facility = TestData.facility(uuid = UUID.fromString("5b9629f3-042b-4b0a-8bd6-f7658130eee7"))
  private val medicalHistoryUuid = UUID.fromString("b0ea3e17-2c6b-4afd-a52b-98a49dfe8147")

  private val dateOfBirthFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)

  private val effectHandler = NewMedicalHistoryEffectHandler(
      schedulersProvider = TrampolineSchedulersProvider(),
      patientRepository = mock(),
      medicalHistoryRepository = mock(),
      dataSync = dataSync,
      currentUser = Lazy { user },
      currentFacility = Lazy { facility },
      uuidGenerator = FakeUuidGenerator.fixed(medicalHistoryUuid),
      dateOfBirthFormatter = dateOfBirthFormatter,
      viewEffectsConsumer = viewEffectHandler::handle
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the load current facility effect is received, the current facility should be loaded`() {
    // when
    testCase.dispatch(LoadCurrentFacility)

    // then
    testCase.assertOutgoingEvents(CurrentFacilityLoaded(facility))
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when the trigger sync effect is received, the frequent syncs must be triggered`() {
    // when
    val patientUuid = UUID.fromString("01a72d61-d502-447d-800f-02a38784fd6e")
    testCase.dispatch(TriggerSync(patientUuid))

    // then
    verify(dataSync).fireAndForgetSync()
    verifyNoMoreInteractions(dataSync)
    testCase.assertOutgoingEvents(SyncTriggered(patientUuid))
    verifyNoInteractions(uiActions)
  }

  @Test
  fun `when show select ongoing hypertension treatment error effect is received, then show ongoing hypertension treatment error dialog`() {
    // when
    testCase.dispatch(ShowOngoingHypertensionTreatmentError)

    // then
    verify(uiActions).showOngoingHypertensionTreatmentErrorDialog()
    verifyNoMoreInteractions(uiActions)

    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when show diagnosis required effect is received, then show diagnosis required error dialog`() {
    // when
    testCase.dispatch(ShowDiagnosisRequiredError)

    // then
    verify(uiActions).showDiagnosisRequiredErrorDialog()
    verifyNoMoreInteractions(uiActions)

    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when show hypertension diagnosis required effect is received, then show diagnosis required error dialog`() {
    // when
    testCase.dispatch(ShowHypertensionDiagnosisRequiredError)

    // then
    verify(uiActions).showHypertensionDiagnosisRequiredErrorDialog()
    verifyNoMoreInteractions(uiActions)

    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when show change diagnosis error effect is received, then show change diagnosis error dialog`() {
    // when
    testCase.dispatch(ShowChangeDiagnosisErrorDialog)

    // then
    verify(uiActions).showChangeDiagnosisErrorDialog()
    verifyNoMoreInteractions(uiActions)

    testCase.assertNoOutgoingEvents()
  }

  @Test
  fun `when show select ongoing diabetes treatment error effect is received, then show ongoing diabetes treatment error dialog`() {
    // when
    testCase.dispatch(ShowOngoingDiabetesTreatmentErrorDialog)

    // then
    verify(uiActions).showOngoingDiabetesTreatmentErrorDialog()
    verifyNoMoreInteractions(uiActions)

    testCase.assertNoOutgoingEvents()
  }
}
