package org.simple.clinic

import org.simple.clinic.facility.Facility
import org.simple.clinic.facility.FacilityConfig
import org.simple.clinic.patient.SyncStatus
import java.time.Instant
import java.util.UUID

const val MAX_ALLOWED_PATIENT_AGE: Int = 120
const val MIN_ALLOWED_PATIENT_AGE: Int = 1
const val SECURITY_PIN_LENGTH: Int = 4
const val LOGIN_OTP_LENGTH = 6
const val INDIA_NHID_LENGTH = 14
const val DEMO_USER_PHONE_NUMBER = "0000000000"
const val DEMO_USER_PIN = "0000"
val DEMO_USER_ID = UUID.fromString("82646d4c-c7ac-4402-b765-2af0575f3ef3")
val DEMO_FACILITY_ID = UUID.fromString("6efec9bf-3fdc-4893-8170-4d6699df2150")
val DEMO_FACILITY = Facility(
    uuid = DEMO_FACILITY_ID,
    name = "Demo facility",
    facilityType = null,
    streetAddress = "",
    villageOrColony = null,
    district = "Krishna",
    state = "Andhra Pradesh",
    country = "IN",
    pinCode = "520010",
    protocolUuid = UUID.fromString("b08d7db1-a336-4561-98e2-ffa4d311027f"),
    groupUuid = UUID.fromString("13af3693-63fb-4e86-a061-1d43bc55736f"), // Default group UUID in prod
    location = null,
    createdAt = Instant.parse("2018-01-01T00:00:00Z"),
    updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
    syncStatus = SyncStatus.DONE,
    deletedAt = null,
    config = FacilityConfig(
        diabetesManagementEnabled = true,
        teleconsultationEnabled = false
    ),
    syncGroup = ""
)
