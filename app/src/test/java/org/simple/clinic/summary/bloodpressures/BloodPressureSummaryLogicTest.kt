package org.simple.clinic.summary.bloodpressures

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
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.summary.PatientSummaryConfig
import org.simple.clinic.widgets.ScreenCreated
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import java.util.UUID

class BloodPressureSummaryLogicTest {

  private val patientUuid = UUID.fromString("d6fc367d-e298-4945-9ed8-b2a86f4a8bbe")
  private val numberOfBpsToDisplay = 100
  private val config = PatientSummaryConfig(numberOfBpPlaceholders = 0, numberOfBpsToDisplay = numberOfBpsToDisplay, bpEditableDuration = Duration.ZERO)
  private val repository = mock<BloodPressureRepository>()
  private val ui = mock<BloodPressureSummaryUi>()
  private val events = PublishSubject.create<UiEvent>()

  lateinit var controller: BloodPressureSummaryViewController
  lateinit var controllerSubscription: Disposable

  @After
  fun tearDown() {
    controllerSubscription.dispose()
  }

  @Test
  fun `the patients blood pressure history must be populated`() {
    // given
    val bloodPressureMeasurements = listOf(
        PatientMocker.bp(patientUuid, systolic = 120, diastolic = 85, recordedAt = Instant.parse("2018-01-01T00:00:00Z")),
        PatientMocker.bp(patientUuid, systolic = 164, diastolic = 95, recordedAt = Instant.parse("2018-01-02T00:01:00Z")),
        PatientMocker.bp(patientUuid, systolic = 144, diastolic = 90, recordedAt = Instant.parse("2018-01-03T00:02:00Z")))

    whenever(repository.newestMeasurementsForPatient(patientUuid, numberOfBpsToDisplay)) doReturn Observable.just(bloodPressureMeasurements)

    // when
    setupController()

    // then
    verify(ui).populateBloodPressures(bloodPressureMeasurements)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when new BP is clicked then BP entry sheet should be shown`() {
    // given
    whenever(repository.newestMeasurementsForPatient(patientUuid, numberOfBpsToDisplay)) doReturn Observable.never<List<BloodPressureMeasurement>>()

    // when
    setupController()
    events.onNext(NewBloodPressureClicked)

    // then
    verify(ui).showBloodPressureEntrySheet(patientUuid)
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when BP is clicked then BP update sheet should be shown`() {
    // given
    whenever(repository.newestMeasurementsForPatient(patientUuid, numberOfBpsToDisplay)) doReturn Observable.never<List<BloodPressureMeasurement>>()

    // when
    setupController()
    val bloodPressureMeasurement = PatientMocker.bp(
        uuid = UUID.fromString("81605b55-b8aa-409d-80a5-42e3e495b3c2"),
        patientUuid = patientUuid
    )
    events.onNext(BloodPressureClicked(bloodPressureMeasurement))

    verify(ui).showBloodPressureUpdateSheet(bloodPressureMeasurement.uuid)
    verifyNoMoreInteractions(ui)
  }

  private fun setupController() {
    controller = BloodPressureSummaryViewController(patientUuid, config, repository)
    controllerSubscription = events.compose(controller).subscribe { it.invoke(ui) }

    events.onNext(ScreenCreated())
  }
}
