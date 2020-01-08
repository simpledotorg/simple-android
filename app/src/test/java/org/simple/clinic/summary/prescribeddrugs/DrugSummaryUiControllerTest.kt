package org.simple.clinic.summary.prescribeddrugs

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import junitparams.Parameters
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.drugs.PrescriptionRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.summary.OpenIntention
import org.simple.clinic.summary.PatientSummaryScreenUi
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

class DrugSummaryUiControllerTest {

  private val patientUuid = UUID.fromString("f5dfca05-59da-4b91-9743-84d2690844c1")

  private val ui = mock<PatientSummaryScreenUi>()
  private val drugSummaryUi = mock<DrugSummaryUi>()
  private val repository = mock<PrescriptionRepository>()
  private val events = PublishSubject.create<UiEvent>()

  lateinit var controller: DrugSummaryUiController
  lateinit var controllerSubscription: Disposable

  @Before
  fun setUp() {
    whenever(ui.drugSummaryUi()) doReturn drugSummaryUi
  }

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `patient's prescription summary should be populated`() {
    // given
    val prescriptions = listOf(
        PatientMocker.prescription(
            uuid = UUID.fromString("b99c147c-d2db-4b73-9b8e-cf8866b7daa1"),
            name = "Amlodipine",
            dosage = "10mg"
        ),
        PatientMocker.prescription(
            uuid = UUID.fromString("262752e2-1565-4140-98d8-0b1e914dbb64"),
            name = "Telmisartan",
            dosage = "9000mg"
        ),
        PatientMocker.prescription(
            uuid = UUID.fromString("f0674dbd-6981-4b79-ae3c-8b513ba1166c"),
            name = "Randomzole",
            dosage = "2 packets"
        )
    )
    whenever(repository.newestPrescriptionsForPatient(patientUuid)) doReturn Observable.just(prescriptions)

    // when
    setupController()

    // then
    verify(drugSummaryUi).populatePrescribedDrugs(prescriptions)
    verifyNoMoreInteractions(drugSummaryUi)
  }

  @Test
  fun `when update medicines is clicked then updated prescription screen should be shown`() {
    // given
    whenever(repository.newestPrescriptionsForPatient(patientUuid)) doReturn Observable.never<List<PrescribedDrug>>()

    // when
    setupController()
    events.onNext(PatientSummaryUpdateDrugsClicked())

    verify(drugSummaryUi).showUpdatePrescribedDrugsScreen(patientUuid)
  }

  private fun setupController() {
    controller = DrugSummaryUiController(patientUuid, repository)

    controllerSubscription = events.compose(controller).subscribe { it.invoke(ui) }

    events.onNext(ScreenCreated())
  }
}
