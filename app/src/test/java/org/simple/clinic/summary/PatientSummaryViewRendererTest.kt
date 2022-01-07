package org.simple.clinic.summary

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.summary.OpenIntention.ViewExistingPatient
import org.simple.clinic.user.User
import java.util.UUID

class PatientSummaryViewRendererTest {

  private val facilityWithDiabetesManagementEnabled = TestData.facility(
      uuid = UUID.fromString("9eb182ee-1ec8-4d19-89b8-abe66ed993d9"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = true)
  )

  private val facilityWithDiabetesManagementDisabled = TestData.facility(
      uuid = UUID.fromString("9eb182ee-1ec8-4d19-89b8-abe66ed993d9"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = false)
  )

  private val facilityWithTeleconsultationEnabled = TestData.facility(
      uuid = UUID.fromString("138ad942-0b8d-4aff-868d-96e98b15dcc3"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = true, teleconsultationEnabled = true)
  )

  private val facilityWithTeleconsultationDisabled = TestData.facility(
      uuid = UUID.fromString("138ad942-0b8d-4aff-868d-96e98b15dcc3"),
      facilityConfig = FacilityConfig(diabetesManagementEnabled = true, teleconsultationEnabled = false)
  )

  private val defaultModel = PatientSummaryModel.from(ViewExistingPatient, UUID.fromString("6fdf088e-f6aa-40e9-9cc2-22e197b83470"))
  private val ui = mock<PatientSummaryScreenUi>()

  private val uiRenderer = PatientSummaryViewRenderer(ui, isNextAppointmentFeatureEnabled = false) { /* no-op */ }

  @Test
  fun `when the facility supports diabetes management, the diabetes widget must be shown`() {
    // given
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showDiabetesView()
    verify(ui).hideTeleconsultButton()
    verify(ui).hideNextAppointmentCard()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the facility does not support diabetes management, the diabetes widget must be hidden`() {
    // given
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementDisabled)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).hideDiabetesView()
    verify(ui).hideTeleconsultButton()
    verify(ui).hideNextAppointmentCard()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when profile summary is loaded, then populate patient profile and show edit button`() {
    // given
    val patientUuid = UUID.fromString("873e001f-fdc7-4e27-a734-5c9f15b22cdc")
    val patient = TestData.patient(
        uuid = patientUuid,
        status = PatientStatus.Active
    )
    val patientAddress = TestData.patientAddress(patient.addressUuid)
    val phoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid)
    val bpPassport = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("526 780", Identifier.IdentifierType.BpPassport))
    val bangladeshNationalId = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("123456789012", Identifier.IdentifierType.BangladeshNationalId))
    val facility = TestData.facility(uuid = UUID.fromString("fe559cb0-f76c-4f34-a3c7-4e696ae2883c"))

    val patientSummaryProfile = PatientSummaryProfile(
        patient = patient,
        address = patientAddress,
        phoneNumber = phoneNumber,
        bpPassport = bpPassport,
        alternativeId = bangladeshNationalId,
        facility = facility
    )


    val model = defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).populatePatientProfile(patientSummaryProfile)
    verify(ui).showEditButton()
    verify(ui).hideAssignedFacilityView()
    verify(ui).hidePatientDiedStatus()
    verify(ui).hideNextAppointmentCard()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when there are no medical officers, then hide contact doctor button`() {
    // given
    val model = defaultModel
        .userLoggedInStatusLoaded(User.LoggedInStatus.LOGGED_IN)
        .currentFacilityLoaded(facilityWithTeleconsultationEnabled)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showDiabetesView()
    verify(ui).hideTeleconsultButton()
    verify(ui).hideNextAppointmentCard()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show contact doctor button if teleconsultation is enabled and user is logged in`() {
    // given
    val medicalOfficers = listOf(
        TestData.medicalOfficer(id = UUID.fromString("621ec868-1559-42f2-a142-634636a1bb01"))
    )
    val model = defaultModel
        .userLoggedInStatusLoaded(User.LoggedInStatus.LOGGED_IN)
        .currentFacilityLoaded(facilityWithTeleconsultationEnabled)
        .medicalOfficersLoaded(medicalOfficers)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showDiabetesView()
    verify(ui).showTeleconsultButton()
    verify(ui).hideNextAppointmentCard()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `hide contact doctor button if teleconsultation is disabled or user is not logged in`() {
    // given
    val model = defaultModel
        .userLoggedInStatusLoaded(User.LoggedInStatus.LOGGED_IN)
        .currentFacilityLoaded(facilityWithTeleconsultationDisabled)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showDiabetesView()
    verify(ui).hideTeleconsultButton()
    verify(ui).hideNextAppointmentCard()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show assigned facility view if patient has assigned facility`() {
    // given
    val patientUuid = UUID.fromString("873e001f-fdc7-4e27-a734-5c9f15b22cdc")
    val patient = TestData.patient(
        uuid = patientUuid,
        assignedFacilityId = UUID.fromString("170049b2-9a97-4da4-a46c-d791751819fd"),
        status = PatientStatus.Active
    )
    val patientAddress = TestData.patientAddress(patient.addressUuid)
    val phoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid)
    val bpPassport = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("526 780", Identifier.IdentifierType.BpPassport))
    val bangladeshNationalId = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("123456789012", Identifier.IdentifierType.BangladeshNationalId))
    val facility = TestData.facility(uuid = UUID.fromString("fe559cb0-f76c-4f34-a3c7-4e696ae2883c"))

    val patientSummaryProfile = PatientSummaryProfile(
        patient = patient,
        address = patientAddress,
        phoneNumber = phoneNumber,
        bpPassport = bpPassport,
        alternativeId = bangladeshNationalId,
        facility = facility
    )


    val model = defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).populatePatientProfile(patientSummaryProfile)
    verify(ui).showEditButton()
    verify(ui).showAssignedFacilityView()
    verify(ui).hidePatientDiedStatus()
    verify(ui).hideNextAppointmentCard()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `hide assigned facility view if patient doesn't have assigned facility`() {
    // given
    val patientUuid = UUID.fromString("873e001f-fdc7-4e27-a734-5c9f15b22cdc")
    val patient = TestData.patient(
        uuid = patientUuid,
        assignedFacilityId = null,
        status = PatientStatus.Active
    )
    val patientAddress = TestData.patientAddress(patient.addressUuid)
    val phoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid)
    val bpPassport = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("526 780", Identifier.IdentifierType.BpPassport))
    val bangladeshNationalId = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("123456789012", Identifier.IdentifierType.BangladeshNationalId))
    val facility = TestData.facility(uuid = UUID.fromString("fe559cb0-f76c-4f34-a3c7-4e696ae2883c"))

    val patientSummaryProfile = PatientSummaryProfile(
        patient = patient,
        address = patientAddress,
        phoneNumber = phoneNumber,
        bpPassport = bpPassport,
        alternativeId = bangladeshNationalId,
        facility = facility
    )


    val model = defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).populatePatientProfile(patientSummaryProfile)
    verify(ui).showEditButton()
    verify(ui).hideAssignedFacilityView()
    verify(ui).hidePatientDiedStatus()
    verify(ui).hideNextAppointmentCard()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when open intention is view existing patient with teleconsultation log, then show teleconsult log button`() {
    // given
    val patientUuid = UUID.fromString("66bc73a4-29a1-4b07-8be1-9223dc5d7cbb")
    val model = PatientSummaryModel.from(
        openIntention = OpenIntention.ViewExistingPatientWithTeleconsultLog(
            teleconsultRecordId = UUID.fromString("726fc0e5-9638-4ce2-a8ca-c14bc72852b5")
        ),
        patientUuid = patientUuid
    ).currentFacilityLoaded(facilityWithTeleconsultationEnabled)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showDiabetesView()
    verify(ui).hideTeleconsultButton()
    verify(ui).hideDoneButton()
    verify(ui).showTeleconsultLogButton()
    verify(ui).hideNextAppointmentCard()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient is not dead, then hide patient died status view`() {
    // given
    val patientUuid = UUID.fromString("53223c27-405c-4bbf-87e7-0bb35207c955")
    val patient = TestData.patient(
        uuid = patientUuid,
        status = PatientStatus.Active
    )
    val patientAddress = TestData.patientAddress(patient.addressUuid)
    val phoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid)
    val bpPassport = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("526 780", Identifier.IdentifierType.BpPassport))
    val bangladeshNationalId = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("123456789012", Identifier.IdentifierType.BangladeshNationalId))
    val facility = TestData.facility(uuid = UUID.fromString("a8a541d3-7cc4-492b-92f3-4fac3e4b88f8"))

    val patientSummaryProfile = PatientSummaryProfile(
        patient = patient,
        address = patientAddress,
        phoneNumber = phoneNumber,
        bpPassport = bpPassport,
        alternativeId = bangladeshNationalId,
        facility = facility
    )

    val model = defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).populatePatientProfile(patientSummaryProfile)
    verify(ui).showEditButton()
    verify(ui).hideAssignedFacilityView()
    verify(ui).hidePatientDiedStatus()
    verify(ui).hideNextAppointmentCard()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient is dead, then show patient died status view`() {
    // given
    val patientUuid = UUID.fromString("830a8adc-b4d1-4b30-8b22-e8ee2412693b")
    val patient = TestData.patient(
        uuid = patientUuid,
        status = PatientStatus.Dead
    )
    val patientAddress = TestData.patientAddress(patient.addressUuid)
    val phoneNumber = TestData.patientPhoneNumber(patientUuid = patientUuid)
    val bpPassport = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("526 780", Identifier.IdentifierType.BpPassport))
    val bangladeshNationalId = TestData.businessId(patientUuid = patientUuid, identifier = Identifier("123456789012", Identifier.IdentifierType.BangladeshNationalId))
    val facility = TestData.facility(uuid = UUID.fromString("0dae915f-7a4f-458d-9434-10d783ec3f33"))

    val patientSummaryProfile = PatientSummaryProfile(
        patient = patient,
        address = patientAddress,
        phoneNumber = phoneNumber,
        bpPassport = bpPassport,
        alternativeId = bangladeshNationalId,
        facility = facility
    )

    val model = defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).populatePatientProfile(patientSummaryProfile)
    verify(ui).showEditButton()
    verify(ui).hideAssignedFacilityView()
    verify(ui).showPatientDiedStatus()
    verify(ui).hideNextAppointmentCard()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient registration data is present and next appointment feature is enabled, then show the next appointment card`() {
    // given
    val modelWithPatientRegistrationData = defaultModel.patientRegistrationDataLoaded(hasPatientRegistrationData = true)
    val uiRenderer = PatientSummaryViewRenderer(ui = ui, isNextAppointmentFeatureEnabled = true) { /* no-op */ }

    // when
    uiRenderer.render(modelWithPatientRegistrationData)

    // then
    verify(ui).showNextAppointmentCard()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient registration data is present and next appointment feature is disabled, then hide the next appointment card`() {
    // given
    val modelWithPatientRegistrationData = defaultModel.patientRegistrationDataLoaded(hasPatientRegistrationData = true)
    val uiRenderer = PatientSummaryViewRenderer(ui = ui, isNextAppointmentFeatureEnabled = false) { /* no-op */ }

    // when
    uiRenderer.render(modelWithPatientRegistrationData)

    // then
    verify(ui).hideNextAppointmentCard()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when patient registration data is not present, then hide the next appointment card`() {
    // given
    val modelWithPatientRegistrationData = defaultModel
        .patientRegistrationDataLoaded(hasPatientRegistrationData = false)

    // when
    uiRenderer.render(modelWithPatientRegistrationData)

    // then
    verify(ui).hideNextAppointmentCard()
    verifyNoMoreInteractions(ui)
  }
}
