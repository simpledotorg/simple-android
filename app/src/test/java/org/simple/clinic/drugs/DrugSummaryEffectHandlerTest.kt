package org.simple.clinic.drugs

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import dagger.Lazy
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.summary.prescribeddrugs.DrugSummaryEffectHandler
import org.simple.clinic.summary.prescribeddrugs.DrugSummaryUiActions
import org.simple.clinic.summary.prescribeddrugs.OpenUpdatePrescribedDrugScreen
import org.simple.clinic.util.scheduler.TestSchedulersProvider
import java.util.UUID

class DrugSummaryEffectHandlerTest {
  private val prescriptionRepository = mock<PrescriptionRepository>()
  private val uiActions = mock<DrugSummaryUiActions>()
  private val facility = TestData.facility(uuid = UUID.fromString("94db5d90-d483-4755-892a-97fde5a870fe"))

  private val effectHandler = DrugSummaryEffectHandler(
      prescriptionRepository = prescriptionRepository,
      schedulersProvider = TestSchedulersProvider.trampoline(),
      uiActions = uiActions,
      currentFacility = Lazy { facility },
  )

  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when open update prescribed drug screen effect is received, then show update prescribed drugs screen`() {
    //given
    val patientUuid = UUID.fromString("67bde563-2cde-4f43-91b4-ba450f0f4d8a")

    //when
    testCase.dispatch(OpenUpdatePrescribedDrugScreen(patientUuid, facility))

    //then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showUpdatePrescribedDrugsScreen(patientUuid, facility)
    verifyNoMoreInteractions(uiActions)
  }
}
