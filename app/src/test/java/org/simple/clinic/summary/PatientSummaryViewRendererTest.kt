package org.simple.clinic.summary

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.summary.OpenIntention.ViewExistingPatient
import org.simple.clinic.summary.teleconsultation.api.TeleconsultInfo
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
    verify(ui).hideContactDoctorButton()
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
    verify(ui).hideContactDoctorButton()
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

    val patientSummaryProfile = PatientSummaryProfile(
        patient = patient,
        address = patientAddress,
        phoneNumber = phoneNumber,
        bpPassport = bpPassport,
        alternativeId = bangladeshNationalId
    )


    val model = defaultModel.patientSummaryProfileLoaded(patientSummaryProfile)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).populatePatientProfile(patientSummaryProfile)
    verify(ui).showEditButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when teleconsultation info is loaded, then enable contact doctor button`() {
    // given
    val phoneNumber = "+911111111111"
    val phoneNumbers = listOf(TestData.teleconsultPhoneNumber(phoneNumber))
    val teleconsultInfo = TeleconsultInfo.Fetched(phoneNumber, phoneNumbers)
    val model = defaultModel
        .currentFacilityLoaded(facilityWithTeleconsultationEnabled)
        .fetchedTeleconsultationInfo(teleconsultInfo)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showDiabetesView()
    verify(ui).hideContactDoctorButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when teleconsultation phone number is missing, then disable contact doctor button`() {
    // given
    val model = defaultModel
        .userLoggedInStatusLoaded(User.LoggedInStatus.LOGGED_IN)
        .currentFacilityLoaded(facilityWithTeleconsultationEnabled)
        .fetchedTeleconsultationInfo(TeleconsultInfo.MissingPhoneNumber)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showDiabetesView()
    verify(ui).showContactDoctorButton()
    verify(ui).showContactDoctorButtonTextAndIcon()
    verify(ui).disableContactDoctorButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when there is a network error when fetching tele consult info, then disable contact doctor button`() {
    // given
    val model = defaultModel
        .userLoggedInStatusLoaded(User.LoggedInStatus.LOGGED_IN)
        .currentFacilityLoaded(facilityWithTeleconsultationEnabled)
        .fetchedTeleconsultationInfo(TeleconsultInfo.NetworkError)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showDiabetesView()
    verify(ui).showContactDoctorButton()
    verify(ui).showContactDoctorButtonTextAndIcon()
    verify(ui).disableContactDoctorButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when teleconsult info is being fetched, then show contact button progress`() {
    // given
    val model = defaultModel
        .userLoggedInStatusLoaded(User.LoggedInStatus.LOGGED_IN)
        .currentFacilityLoaded(facilityWithTeleconsultationEnabled)
        .fetchingTeleconsultationInfo()

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showDiabetesView()
    verify(ui).showContactDoctorButton()
    verify(ui).showContactButtonProgress()
    verify(ui).enableContactDoctorButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `show contact doctor button if teleconsultation is enabled and user is logged in`() {
    // given
    val model = defaultModel
        .userLoggedInStatusLoaded(User.LoggedInStatus.LOGGED_IN)
        .currentFacilityLoaded(facilityWithTeleconsultationEnabled)

    // when
    uiRenderer.render(model)

    // then
    verify(ui).showDiabetesView()
    verify(ui).showContactDoctorButton()
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
    verify(ui).hideContactDoctorButton()
    verifyNoMoreInteractions(ui)
  }
}
