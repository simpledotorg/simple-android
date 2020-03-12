package org.simple.clinic.summary

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.After
import org.junit.Test
import org.simple.clinic.bloodsugar.BloodSugarRepository
import org.simple.clinic.bp.BloodPressureRepository
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientProfile
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.*
import org.simple.clinic.sync.DataSync
import org.simple.clinic.sync.SyncGroup.FREQUENT
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.threeten.bp.Instant
import java.util.UUID

class PatientSummaryEffectHandlerTest {

  private val uiActions = mock<PatientSummaryUiActions>()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val bloodSugarRepository = mock<BloodSugarRepository>()
  private val bloodPressureRepository = mock<BloodPressureRepository>()
  private val dataSync = mock<DataSync>()

  private val effectHandler = PatientSummaryEffectHandler(
      schedulersProvider = TrampolineSchedulersProvider(),
      patientRepository = patientRepository,
      bloodPressureRepository = bloodPressureRepository,
      appointmentRepository = mock(),
      missingPhoneReminderRepository = mock(),
      userSession = userSession,
      facilityRepository = facilityRepository,
      bloodSugarRepository = bloodSugarRepository,
      dataSync = dataSync,
      uiActions = uiActions
  )
  private val testCase = EffectHandlerTestCase(effectHandler.build())

  @After
  fun tearDown() {
    testCase.dispose()
  }

  @Test
  fun `when the load current facility effect is received, the current facility must be fetched`() {
    // given
    val user = PatientMocker.loggedInUser(uuid = UUID.fromString("39f96341-c043-4059-880e-e32754341a04"))
    val facility = PatientMocker.facility(uuid = UUID.fromString("94db5d90-d483-4755-892a-97fde5a870fe"))

    whenever(userSession.loggedInUserImmediate()) doReturn user
    whenever(facilityRepository.currentFacility(user)) doReturn Observable.just(facility)

    // when
    testCase.dispatch(LoadCurrentFacility)

    // then
    testCase.assertOutgoingEvents(CurrentFacilityLoaded(facility))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the load patient summary profile is received, then patient summary profile must be fetched`() {
    // given
    val patientUuid = UUID.fromString("2bd2ad18-f532-4649-b775-efe97c38ce59")
    val patient = PatientMocker.patient(patientUuid)
    val patientAddress = PatientMocker.address(uuid = patient.addressUuid)
    val patientPhoneNumber = PatientMocker.phoneNumber(patientUuid = patientUuid)
    val bpPassport = PatientMocker.businessId(patientUuid = patientUuid, identifier = Identifier("526 780", BpPassport))
    val bangladeshNationId = PatientMocker.businessId(patientUuid = patientUuid, identifier = Identifier("123456789012", BangladeshNationalId))

    val patientProfile = PatientProfile(patient, patientAddress, listOf(patientPhoneNumber), listOf(bangladeshNationId, bpPassport))
    whenever(patientRepository.patientProfileImmediate(patientUuid)) doReturn Just(patientProfile)

    // when
    testCase.dispatch(LoadPatientSummaryProfile(patientUuid))

    // then
    testCase.assertOutgoingEvents(PatientSummaryProfileLoaded(
        PatientSummaryProfile(
            patient = patient,
            address = patientAddress,
            phoneNumber = patientPhoneNumber,
            bpPassport = bpPassport,
            bangladeshNationalId = bangladeshNationId
        )
    ))
  }

  @Test
  fun `when edit click effect is received then show edit patient screen`() {
    //given
    val patientProfile = PatientMocker.patientProfile(
        patientUuid = UUID.fromString("12fdada1-57df-49de-871a-766fbdbb2f37"),
        addressUuid = UUID.fromString("d261cde2-b0cb-436e-9612-8b3b7bde0c63")
    )
    val patientSummaryProfile = PatientSummaryProfile(
        patient = patientProfile.patient,
        phoneNumber = null,
        address = patientProfile.address,
        bpPassport = null,
        bangladeshNationalId = null
    )

    //when
    testCase.dispatch(HandleEditClick(patientSummaryProfile))

    //then
    testCase.assertNoOutgoingEvents()
    verify(uiActions).showEditPatientScreen(patientSummaryProfile)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when the load data for back click effect is received, load the data`() {
    // given
    val screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
    val patientUuid = UUID.fromString("67bde563-2cde-4f43-91b4-ba450f0f4d8a")

    whenever(patientRepository.hasPatientDataChangedSince(patientUuid, screenCreatedTimestamp)) doReturn true
    whenever(bloodPressureRepository.bloodPressureCountImmediate(patientUuid)) doReturn 0
    whenever(bloodSugarRepository.bloodSugarCountImmediate(patientUuid)) doReturn 1

    // when
    testCase.dispatch(LoadDataForBackClick(patientUuid, screenCreatedTimestamp))

    // then
    testCase.assertOutgoingEvents(DataForBackClickLoaded(
        hasPatientDataChangedSinceScreenCreated = true,
        noBloodPressuresRecordedForPatient = true,
        noBloodSugarsRecordedForPatient = false
    ))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the load data for done click effect is received, load the data`() {
    // given
    val patientUuid = UUID.fromString("67bde563-2cde-4f43-91b4-ba450f0f4d8a")

    whenever(bloodPressureRepository.bloodPressureCountImmediate(patientUuid)) doReturn 1
    whenever(bloodSugarRepository.bloodSugarCountImmediate(patientUuid)) doReturn 0

    // when
    testCase.dispatch(LoadDataForDoneClick(patientUuid))

    // then
    testCase.assertOutgoingEvents(DataForDoneClickLoaded(
        noBloodPressuresRecordedForPatient = false,
        noBloodSugarsRecordedForPatient = true
    ))
    verifyZeroInteractions(uiActions)
  }

  @Test
  fun `when the trigger sync effect is received, trigger a sync`() {
    // when
    testCase.dispatch(TriggerSync(BACK_CLICK))

    // then
    verify(dataSync).fireAndForgetSync(FREQUENT)
    verifyNoMoreInteractions(dataSync)
    testCase.assertOutgoingEvents(SyncTriggered(BACK_CLICK))
    verifyZeroInteractions(uiActions)
  }
}
