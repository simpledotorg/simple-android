package org.simple.clinic.summary.prescribeddrugs

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Test
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.TestData
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class DrugSummaryUiControllerTest {

  private val patientUuid = UUID.fromString("f5dfca05-59da-4b91-9743-84d2690844c1")

  private val ui = mock<DrugSummaryUi>()
  private val repository = mock<PrescriptionRepository>()
  private val events = PublishSubject.create<UiEvent>()

  lateinit var controller: DrugSummaryUiController
  lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `patient's prescription summary should be populated`() {
    // given
    val prescriptions = listOf(
        TestData.prescription(
            uuid = UUID.fromString("b99c147c-d2db-4b73-9b8e-cf8866b7daa1"),
            name = "Amlodipine",
            dosage = "10mg"
        ),
        TestData.prescription(
            uuid = UUID.fromString("262752e2-1565-4140-98d8-0b1e914dbb64"),
            name = "Telmisartan",
            dosage = "9000mg"
        ),
        TestData.prescription(
            uuid = UUID.fromString("f0674dbd-6981-4b79-ae3c-8b513ba1166c"),
            name = "Randomzole",
            dosage = "2 packets"
        )
    )
    whenever(repository.newestPrescriptionsForPatient(patientUuid)) doReturn Observable.just(prescriptions)

    // when
    setupController()

    // then
    verify(ui).populatePrescribedDrugs(prescriptions)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when update medicines is clicked then updated prescription screen should be shown`() {
    // given
    whenever(repository.newestPrescriptionsForPatient(patientUuid)) doReturn Observable.never<List<PrescribedDrug>>()

    // when
    setupController()
    events.onNext(PatientSummaryUpdateDrugsClicked())

    verify(ui).showUpdatePrescribedDrugsScreen(patientUuid)
  }

  private fun setupController() {
    controller = DrugSummaryUiController(patientUuid, repository)

    controllerSubscription = events.compose(controller).subscribe { it.invoke(ui) }

    events.onNext(ScreenCreated())
  }
}
