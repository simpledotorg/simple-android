package org.simple.clinic.summary.teleconsultation.contactdoctor

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.summary.PatientTeleconsultationInfo
import java.util.UUID

class ContactDoctorUpdateTest {

  private val patientUuid = UUID.fromString("ba46b46a-058a-4694-bb02-69aad1694bc7")
  private val model = ContactDoctorModel.create(patientUuid)
  private val updateSpec = UpdateSpec(ContactDoctorUpdate())

  private val teleconsultRecordId = UUID.fromString("edced500-ef21-4749-b2ce-c5ef028edee8")
  private val medicalOfficerId = UUID.fromString("9ae68a58-43ff-42c3-b998-70b5a7589b9d")
  private val doctorPhoneNumber = "+911111111111"

  @Test
  fun `when medical officers are loaded, then update the model`() {
    val medicalOfficers = listOf(
        TestData.medicalOfficer(
            id = UUID.fromString("a07e53e8-959b-4d08-bea7-161932ecc0a5"),
            fullName = "Doctor Doom",
            phoneNumber = "+911111111111"
        )
    )

    updateSpec
        .given(model)
        .whenEvent(MedicalOfficersLoaded(medicalOfficers))
        .then(assertThatNext(
            hasModel(model.medicalOfficersLoaded(medicalOfficers)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when teleconsult request is created, then load patient teleconsult info`() {
    updateSpec
        .given(model)
        .whenEvent(TeleconsultRequestCreated(
            teleconsultRecordId = teleconsultRecordId,
            doctorPhoneNumber = doctorPhoneNumber,
            messageTarget = MessageTarget.SMS
        ))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(LoadPatientTeleconsultInfo(
                patientUuid = patientUuid,
                teleconsultRecordId = teleconsultRecordId,
                doctorPhoneNumber = doctorPhoneNumber,
                messageTarget = MessageTarget.SMS
            ))
        ))
  }

  @Test
  fun `when patient teleconsult info is loaded, then send teleconsult message`() {
    val facilityUuid = UUID.fromString("ffae95ae-ae05-4bdc-8103-3ee8e927713c")
    val facility = TestData.facility(uuid = facilityUuid)
    val user = TestData.loggedInUser(uuid = UUID.fromString("a072f41a-9990-400f-b293-b282a6f4b8ff"))

    val bpPassport = TestData.businessId(
        uuid = UUID.fromString("7ebf2c49-8018-41b9-af7c-43afe02483d4"),
        patientUuid = patientUuid,
        identifier = Identifier("1234567", Identifier.IdentifierType.BpPassport),
        metaDataVersion = BusinessId.MetaDataVersion.BpPassportMetaDataV1
    )

    val bloodPressure1 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("ad827cfa-1436-4b98-88be-28831543b9f9"),
        patientUuid = patientUuid,
        facilityUuid = facilityUuid
    )
    val bloodPressure2 = TestData.bloodPressureMeasurement(
        uuid = UUID.fromString("95aaf3f5-cafe-4b1f-a91a-14cbae2de12a"),
        patientUuid = patientUuid,
        facilityUuid = facilityUuid
    )
    val bloodPressures = listOf(bloodPressure1, bloodPressure2)

    val bloodSugar1 = TestData.bloodSugarMeasurement(
        uuid = UUID.fromString("81dc9be6-481e-4da0-a975-1998f2850562"),
        patientUuid = patientUuid,
        facilityUuid = facilityUuid
    )

    val bloodSugars = listOf(bloodSugar1)

    val prescription = TestData.prescription(
        uuid = UUID.fromString("38fffa68-bc29-4a67-a6c8-ece61071fe3b"),
        patientUuid = patientUuid
    )
    val prescriptions = listOf(prescription)

    val medicalHistoryUuid = UUID.fromString("4d72f266-0da5-4418-82e7-238f2fcabcb3")
    val medicalHistory = TestData.medicalHistory(
        uuid = medicalHistoryUuid,
        patientUuid = patientUuid,
        diagnosedWithHypertension = Answer.Yes,
        hasDiabetes = Answer.No
    )

    val patientTeleconsultInfo = PatientTeleconsultationInfo(
        patientUuid = patientUuid,
        teleconsultRecordId = teleconsultRecordId,
        bpPassport = bpPassport.identifier.displayValue(),
        facility = facility,
        bloodPressures = bloodPressures,
        bloodSugars = bloodSugars,
        prescriptions = prescriptions,
        medicalHistory = medicalHistory,
        nursePhoneNumber = user.phoneNumber,
        doctorPhoneNumber = doctorPhoneNumber
    )

    updateSpec
        .given(model)
        .whenEvent(PatientTeleconsultInfoLoaded(patientTeleconsultInfo, MessageTarget.SMS))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SendTeleconsultMessage(patientTeleconsultInfo, MessageTarget.SMS))
        ))
  }

  @Test
  fun `when whats app button is clicked, then create teleconsult record`() {
    updateSpec
        .given(model)
        .whenEvent(WhatsAppButtonClicked(medicalOfficerId, doctorPhoneNumber))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CreateTeleconsultRequest(
                patientUuid = patientUuid,
                medicalOfficerId = medicalOfficerId,
                doctorPhoneNumber = doctorPhoneNumber,
                messageTarget = MessageTarget.WHATSAPP
            ))
        ))
  }

  @Test
  fun `when sms button is clicked, then create teleconsult record`() {
    updateSpec
        .given(model)
        .whenEvent(SmsButtonClicked(medicalOfficerId, doctorPhoneNumber))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CreateTeleconsultRequest(
                patientUuid = patientUuid,
                medicalOfficerId = medicalOfficerId,
                doctorPhoneNumber = doctorPhoneNumber,
                messageTarget = MessageTarget.SMS
            ))
        ))
  }
}
