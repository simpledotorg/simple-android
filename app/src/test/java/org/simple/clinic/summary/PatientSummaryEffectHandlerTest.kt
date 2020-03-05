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
import org.simple.clinic.facility.FacilityRepository
import org.simple.clinic.mobius.EffectHandlerTestCase
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.summary.AppointmentSheetOpenedFrom.DONE_CLICK
import org.simple.clinic.summary.OpenIntention.LinkIdWithPatient
import org.simple.clinic.summary.OpenIntention.ViewExistingPatient
import org.simple.clinic.summary.OpenIntention.ViewNewPatient
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.scheduler.TrampolineSchedulersProvider
import org.simple.clinic.util.toOptional
import org.threeten.bp.Instant
import java.util.UUID

class PatientSummaryEffectHandlerTest {

  private val uiActions = mock<PatientSummaryUiActions>()
  private val userSession = mock<UserSession>()
  private val facilityRepository = mock<FacilityRepository>()
  private val patientRepository = mock<PatientRepository>()
  private val bloodSugarRepository = mock<BloodSugarRepository>()

  private val effectHandler = PatientSummaryEffectHandler(
      schedulersProvider = TrampolineSchedulersProvider(),
      patientRepository = patientRepository,
      bloodPressureRepository = mock(),
      appointmentRepository = mock(),
      missingPhoneReminderRepository = mock(),
      userSession = userSession,
      facilityRepository = facilityRepository,
      bloodSugarRepository = bloodSugarRepository,
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

    whenever(patientRepository.patient(patientUuid)) doReturn Observable.just(patient.toOptional())
    whenever(patientRepository.address(patient.addressUuid)) doReturn Observable.just(patientAddress.toOptional())
    whenever(patientRepository.phoneNumber(patientUuid)) doReturn Observable.just(patientPhoneNumber.toOptional())
    whenever(patientRepository.bpPassportForPatient(patientUuid)) doReturn Observable.just(bpPassport.toOptional())
    whenever(patientRepository.bangladeshNationalIdForPatient(patientUuid)) doReturn Observable.just(bangladeshNationId.toOptional())

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
  fun `when there are patient summary changes and at least one blood sugar is present, clicking on back must show the schedule appointment sheet`() {
    // given
    val screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
    val patientUuid = UUID.fromString("67bde563-2cde-4f43-91b4-ba450f0f4d8a")

    whenever(patientRepository.hasPatientDataChangedSince(patientUuid, screenCreatedTimestamp)).doReturn(true)
    whenever(bloodSugarRepository.bloodSugarCountImmediate(patientUuid)).doReturn(1)

    // when
    testCase.dispatch(HandleBackClick(patientUuid, screenCreatedTimestamp, ViewExistingPatient))

    // then
    verify(uiActions).showScheduleAppointmentSheet(patientUuid, AppointmentSheetOpenedFrom.BACK_CLICK)
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when there are patient summary changes and all blood sugars are deleted, clicking on back for existing patient screen must go back to previous screen`() {
    // given
    val screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
    val patientUuid = UUID.fromString("ea1fc96f-4736-400b-829e-e8d40d554669")

    whenever(bloodSugarRepository.bloodSugarCountImmediate(patientUuid)) doReturn 0
    whenever(patientRepository.hasPatientDataChangedSince(patientUuid, screenCreatedTimestamp)) doReturn true

    // when
    testCase.dispatch(HandleBackClick(patientUuid, screenCreatedTimestamp, ViewExistingPatient))

    // then
    verify(uiActions).goToPreviousScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when there are patient summary changes and all blood sugars are deleted, clicking on back for new patient screen must go back to home screen`() {
    // given
    val screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
    val patientUuid = UUID.fromString("ea1fc96f-4736-400b-829e-e8d40d554669")

    whenever(bloodSugarRepository.bloodSugarCountImmediate(patientUuid)) doReturn 0
    whenever(patientRepository.hasPatientDataChangedSince(patientUuid, screenCreatedTimestamp)) doReturn true

    // when
    testCase.dispatch(HandleBackClick(patientUuid, screenCreatedTimestamp, ViewNewPatient))

    // then
    verify(uiActions).goToHomeScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when there are patient summary changes and all blood sugars are deleted, clicking on back link id with patient screen must go back to home screen`() {
    // given
    val screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
    val patientUuid = UUID.fromString("ea1fc96f-4736-400b-829e-e8d40d554669")

    whenever(bloodSugarRepository.bloodSugarCountImmediate(patientUuid)) doReturn 0
    whenever(patientRepository.hasPatientDataChangedSince(patientUuid, screenCreatedTimestamp)) doReturn true

    // when
    testCase.dispatch(HandleBackClick(patientUuid, screenCreatedTimestamp, LinkIdWithPatient(Identifier("927ac52b-a51c-487a-9737-a3150ff73d9d", BpPassport))))

    // then
    verify(uiActions).goToHomeScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when there are no patient summary changes and all blood sugars are not deleted, clicking on back must go back`() {
    // given
    val screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
    val patientUuid = UUID.fromString("eaacf3be-2bc1-46c2-9132-d4f79e5b83ca")

    whenever(bloodSugarRepository.bloodSugarCountImmediate(patientUuid)) doReturn 1
    whenever(patientRepository.hasPatientDataChangedSince(patientUuid, screenCreatedTimestamp)) doReturn false

    // when
    testCase.dispatch(HandleBackClick(patientUuid, screenCreatedTimestamp, ViewExistingPatient))

    // then
    verify(uiActions).goToPreviousScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when there are no patient summary changes and all blood sugars are deleted, clicking on back must go back`() {
    // given
    val screenCreatedTimestamp = Instant.parse("2018-01-01T00:00:00Z")
    val patientUuid = UUID.fromString("81c5537f-34fc-4480-a2dc-a92eacf973e7")

    whenever(bloodSugarRepository.bloodSugarCountImmediate(patientUuid)) doReturn 0
    whenever(patientRepository.hasPatientDataChangedSince(patientUuid, screenCreatedTimestamp)) doReturn false

    // when
    testCase.dispatch(HandleBackClick(patientUuid, screenCreatedTimestamp, ViewExistingPatient))

    // then
    verify(uiActions).goToPreviousScreen()
    verifyNoMoreInteractions(uiActions)
  }

  @Test
  fun `when all blood sugars are not deleted, clicking on save must show the schedule appointment sheet regardless of summary changes`() {
    // given
    val patientUuid = UUID.fromString("14b77a00-3cff-40aa-83da-36547e3c9ef6")

    whenever(bloodSugarRepository.bloodSugarCountImmediate(patientUuid)).doReturn(1)

    // when
    testCase.dispatch(HandleDoneClick(patientUuid))

    // then
    verify(uiActions).showScheduleAppointmentSheet(patientUuid, DONE_CLICK)
    verifyNoMoreInteractions(uiActions)
  }
}
