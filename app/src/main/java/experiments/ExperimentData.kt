package experiments

import org.simple.clinic.facility.FacilityPayload
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.UserStatus
import org.threeten.bp.Instant
import java.util.UUID

object ExperimentData {
  val facilityPayload = listOf(
      FacilityPayload(
          uuid = UUID.fromString("f992fe9a-459e-4dff-87ac-88c50fe9d729"),
          name = "Facility 1",
          facilityType = "PHC",
          streetAddress = "",
          villageOrColony = "",
          district = "Bathinda",
          state = "Punjab",
          country = "India",
          pinCode = "",
          protocolUuid = UUID.fromString("ad7d5e34-4d4a-4dcc-98dc-27e98977d1cc"),
          groupUuid = UUID.fromString("5ed7b8dd-1834-47d8-bed7-a93e476c8785"),
          locationLatitude = 30.381528,
          locationLongitude = 74.978558,
          createdAt = Instant.parse("2018-01-01T00:00:00Z"),
          updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
          deletedAt = null
      ),
      FacilityPayload(
          uuid = UUID.fromString("b12578cf-05b5-4082-ae4e-0c41a056ac03"),
          name = "Facility 2",
          facilityType = "PHC",
          streetAddress = "",
          villageOrColony = "",
          district = "Bathinda",
          state = "Punjab",
          country = "India",
          pinCode = "",
          protocolUuid = UUID.fromString("ad7d5e34-4d4a-4dcc-98dc-27e98977d1cc"),
          groupUuid = UUID.fromString("5ed7b8dd-1834-47d8-bed7-a93e476c8785"),
          locationLatitude = 30.381528,
          locationLongitude = 74.978558,
          createdAt = Instant.parse("2018-01-01T00:00:00Z"),
          updatedAt = Instant.parse("2018-01-01T00:00:00Z"),
          deletedAt = null
      )
  )

  val loggedInUserPayload = LoggedInUserPayload(
      uuid = UUID.fromString("a9e10bef-0978-4363-9d4c-09587cb5805d"),
      fullName = "Test User",
      phoneNumber = "1111111111",
      pinDigest = "\$2a\$10\$HDDWyQS.SNtJ03QnubMBkeIDlLfpxXBQ0pgnuXmUvdtsSQVO4pgze",
      registrationFacilityId = facilityPayload.first().uuid,
      status = UserStatus.DisapprovedForSyncing,
      createdAt = Instant.parse("2018-01-01T00:00:00Z"),
      updatedAt = Instant.parse("2018-01-01T00:00:00Z")
  )

}
