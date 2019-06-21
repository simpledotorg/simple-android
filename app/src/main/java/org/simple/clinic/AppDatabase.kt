package org.simple.clinic

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.facility.Facility
import org.simple.clinic.home.overdue.OverdueAppointment
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.overdue.Appointment
import org.simple.clinic.overdue.AppointmentCancelReason
import org.simple.clinic.overdue.communication.Communication
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientFuzzySearch
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.PatientPhoneNumberType
import org.simple.clinic.patient.PatientSearchResult
import org.simple.clinic.patient.PatientStatus
import org.simple.clinic.patient.RecentPatient
import org.simple.clinic.patient.SyncStatus
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.protocol.Protocol
import org.simple.clinic.protocol.ProtocolDrug
import org.simple.clinic.summary.addphone.MissingPhoneReminder
import org.simple.clinic.user.LoggedInUserFacilityMapping
import org.simple.clinic.user.OngoingLoginEntry
import org.simple.clinic.user.User
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.InstantRoomTypeConverter
import org.simple.clinic.util.LocalDateRoomTypeConverter
import org.simple.clinic.util.UuidRoomTypeConverter

@Database(
    entities = [
      Patient::class,
      PatientAddress::class,
      PatientPhoneNumber::class,
      BloodPressureMeasurement::class,
      PrescribedDrug::class,
      Facility::class,
      User::class,
      LoggedInUserFacilityMapping::class,
      Appointment::class,
      Communication::class,
      MedicalHistory::class,
      OngoingLoginEntry::class,
      Protocol::class,
      ProtocolDrug::class,
      BusinessId::class,
      MissingPhoneReminder::class
    ],
    version = 37,
    exportSchema = true)
@TypeConverters(
    Gender.RoomTypeConverter::class,
    PatientPhoneNumberType.RoomTypeConverter::class,
    PatientStatus.RoomTypeConverter::class,
    SyncStatus.RoomTypeConverter::class,
    UserStatus.RoomTypeConverter::class,
    User.LoggedInStatus.RoomTypeConverter::class,
    Appointment.Status.RoomTypeConverter::class,
    AppointmentCancelReason.RoomTypeConverter::class,
    Communication.Type.RoomTypeConverter::class,
    Communication.Result.RoomTypeConverter::class,
    MedicalHistory.Answer.RoomTypeConverter::class,
    InstantRoomTypeConverter::class,
    LocalDateRoomTypeConverter::class,
    UuidRoomTypeConverter::class,
    Identifier.IdentifierType.RoomTypeConverter::class,
    BusinessId.MetaDataVersion.RoomTypeConverter::class,
    Appointment.AppointmentType.RoomTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {

  private val patientFuzzyPatientSearchDao by lazy {
    PatientFuzzySearch.PatientFuzzySearchDaoImpl(openHelper, patientSearchDao())
  }

  abstract fun patientDao(): Patient.RoomDao

  abstract fun addressDao(): PatientAddress.RoomDao

  abstract fun phoneNumberDao(): PatientPhoneNumber.RoomDao

  abstract fun patientSearchDao(): PatientSearchResult.RoomDao

  abstract fun bloodPressureDao(): BloodPressureMeasurement.RoomDao

  abstract fun prescriptionDao(): PrescribedDrug.RoomDao

  abstract fun facilityDao(): Facility.RoomDao

  abstract fun userDao(): User.RoomDao

  abstract fun userFacilityMappingDao(): LoggedInUserFacilityMapping.RoomDao

  fun fuzzyPatientSearchDao(): PatientFuzzySearch.PatientFuzzySearchDao = patientFuzzyPatientSearchDao

  abstract fun appointmentDao(): Appointment.RoomDao

  abstract fun communicationDao(): Communication.RoomDao

  abstract fun overdueAppointmentDao(): OverdueAppointment.RoomDao

  abstract fun medicalHistoryDao(): MedicalHistory.RoomDao

  abstract fun ongoingLoginEntryDao(): OngoingLoginEntry.RoomDao

  abstract fun protocolDao(): Protocol.RoomDao

  abstract fun protocolDrugDao(): ProtocolDrug.RoomDao

  abstract fun recentPatientDao(): RecentPatient.RoomDao

  abstract fun businessIdDao(): BusinessId.RoomDao

  abstract fun missingPhoneReminderDao(): MissingPhoneReminder.RoomDao
}
