package org.simple.clinic.summary

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityConfig
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

  private val uiRenderer = PatientSummaryViewRenderer(ui)

  @Test
  fun `when the facility supports diabetes management, the diabetes widget must be shown`() {
    // given
    val model = defaultModel.currentFacilityLoaded(facilityWithDiabetesManagementEnabled)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showDiabetesView()
    verify(ui).hideTeleconsultButton()
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
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when profile summary is loaded, then populate patient profile and show edit button`() {
    // given
    val patientUuid = UUID.fromString("873e001f-fdc7-4e27-a734-5c9f15b22cdc")
    val patient = TestData.patient(patientUuid)
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
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show assigned facility view if patient has assigned facility`() {
    // given
    val patientUuid = UUID.fromString("873e001f-fdc7-4e27-a734-5c9f15b22cdc")
    val patient = TestData.patient(uuid = patientUuid, assignedFacilityId = UUID.fromString("170049b2-9a97-4da4-a46c-d791751819fd"))
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
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `hide assigned facility view if patient doesn't have assigned facility`() {
    // given
    val patientUuid = UUID.fromString("873e001f-fdc7-4e27-a734-5c9f15b22cdc")
    val patient = TestData.patient(
        uuid = patientUuid,
        assignedFacilityId = null
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
    verifyNoMoreInteractions(ui)
  }
}
