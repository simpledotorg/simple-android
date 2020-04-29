package org.simple.clinic.bloodsugar.history

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.bloodsugar.BloodSugarHistoryListItemDataSourceFactory
import org.simple.clinic.bloodsugar.BloodSugarMeasurement
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import java.util.UUID

class BloodSugarHistoryScreenEffectHandlerTest {
  private val patientRepository = mock<PatientRepository>()
  private val bloodSugarRepository = mock<BloodSugarRepository>()
  private val patientUuid = UUID.fromString("1d695883-54cf-4cf0-8795-43f83a0c3f02")
  private val uiActions = mock<BloodSugarHistoryScreenUiActions>()
  private val dataSourceFactory = mock<BloodSugarHistoryListItemDataSourceFactory.Factory>()
  private val effectHandler = BloodSugarHistoryScreenEffectHandler(
      patientRepository,
      bloodSugarRepository,
      TrampolineSchedulersProvider(),
      dataSourceFactory,
      uiActions).build()
  private val testCase = EffectHandlerTestCase(effectHandler)

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when load patient effect is received, then load patient`() {
    // given
    val patient = TestData.patient(uuid = patientUuid)
    whenever(patientRepository.patient(patientUuid)) doReturn Observable.just<Optional<Patient>>(Just(patient))

    // when
    testCase.dispatch(LoadPatient(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientLoaded(patient))
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open blood sugar entry sheet effect is received, then open blood sugar entry sheet`() {
    // when
    testCase.dispatch(OpenBloodSugarEntrySheet(patientUuid))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openBloodSugarEntrySheet(patientUuid)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when open blood sugar update sheet effect is received, then open blood sugar update sheet`() {
    // given
    val measurement = TestData.bloodSugarMeasurement()

    // when
    testCase.dispatch(OpenBloodSugarUpdateSheet(measurement))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).openBloodSugarUpdateSheet(measurement)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when show blood sugars effect is received, then show blood sugars`() {
    // given
    val bloodSugarsDataSourceFactory = mock<DataSource.Factory<Int, BloodSugarMeasurement>>()
    val bloodSugarsDataSource = mock<PositionalDataSource<BloodSugarMeasurement>>()
    val bloodSugarsHistoryListItemDataSourceFactory = mock<BloodSugarHistoryListItemDataSourceFactory>()

    whenever(bloodSugarRepository.allBloodSugarsDataSource(patientUuid)).thenReturn(bloodSugarsDataSourceFactory)
    whenever(bloodSugarsDataSourceFactory.create()).thenReturn(bloodSugarsDataSource)
    whenever(dataSourceFactory.create(bloodSugarsDataSource)).thenReturn(bloodSugarsHistoryListItemDataSourceFactory)

    // when
    testCase.dispatch(ShowBloodSugars(patientUuid))

    // then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showBloodSugars(bloodSugarsHistoryListItemDataSourceFactory)
    verifyNoMoreInteractions(uiActions)
  }

}
