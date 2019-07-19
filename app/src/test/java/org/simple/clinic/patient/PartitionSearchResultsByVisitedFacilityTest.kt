package org.simple.clinic.patient

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Flowable
import io.reactivex.Observable
import org.junit.Test
import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.bp.PatientToFacilityId
import org.simple.clinic.searchresultsview.PartitionSearchResultsByVisitedFacility
import org.simple.clinic.searchresultsview.PatientSearchResults
import java.util.UUID

class PartitionSearchResultsByVisitedFacilityTest {

  @Test
  fun `search results should be grouped by the facility`() {
    // given
    val facilityA = PatientMocker.facility(UUID.fromString("39e23001-955a-4771-a3fe-f58d046eeea3"))
    val facilityB = PatientMocker.facility(UUID.fromString("20b88227-6809-49c9-b68b-46d8e1db02cb"))
    val facilityC = PatientMocker.facility(UUID.fromString("55e8052c-3074-4e10-b3a1-0b1ce194faef"))

    val patient1InFacilityA = PatientMocker.patientSearchResult(
        uuid = UUID.fromString("82406b52-6b21-4a71-8114-0ea0bd58a859"),
        fullName = "patient 1 In Facility A"
    )
    val patient2InFacilityA = PatientMocker.patientSearchResult(
        uuid = UUID.fromString("3e6a13e8-d5f9-47cb-960c-b64b651d972b"),
        fullName = "patient 2 In Facility A"
    )
    val patient3InFacilityA = PatientMocker.patientSearchResult(
        uuid = UUID.fromString("629fe81e-43ff-45b1-9081-88ccccefd3b6"),
        fullName = "patient 3 In Facility A"
    )
    val patient1InFacilityB = PatientMocker.patientSearchResult(
        uuid = UUID.fromString("a30e84a5-1545-4f96-8274-1bc826d2ce91"),
        fullName = "patient 1 In Facility B"
    )
    val patient1InFacilityC = PatientMocker.patientSearchResult(
        uuid = UUID.fromString("5a91e476-729d-408a-bfe9-e0793baee83c"),
        fullName = "patient 1 In Facility C"
    )
    val patient2InFacilityC = PatientMocker.patientSearchResult(
        uuid = UUID.fromString("3e29a98a-db88-44cb-9b5b-fbda2204a351"),
        fullName = "patient 2 In Facility C"
    )

    val patientSearchResults = listOf(
        patient1InFacilityA,
        patient2InFacilityC,
        patient3InFacilityA,
        patient1InFacilityB,
        patient1InFacilityC,
        patient2InFacilityA
    )

    val patientUuids = patientSearchResults.map { it.uuid }

    val bloodPressureDao = mock<BloodPressureMeasurement.RoomDao>()
    whenever(bloodPressureDao.patientToFacilityIds(patientUuids))
        .thenReturn(Flowable.just(listOf(
            PatientToFacilityId(patientUuid = patient1InFacilityA.uuid, facilityUuid = facilityA.uuid),
            PatientToFacilityId(patientUuid = patient2InFacilityA.uuid, facilityUuid = facilityA.uuid),
            PatientToFacilityId(patientUuid = patient3InFacilityA.uuid, facilityUuid = facilityA.uuid),
            PatientToFacilityId(patientUuid = patient1InFacilityB.uuid, facilityUuid = facilityB.uuid),
            PatientToFacilityId(patientUuid = patient1InFacilityC.uuid, facilityUuid = facilityC.uuid),
            PatientToFacilityId(patientUuid = patient2InFacilityC.uuid, facilityUuid = facilityC.uuid)
        )))

    // when
    val testObserver = Observable.just(patientSearchResults)
        .compose(PartitionSearchResultsByVisitedFacility(bloodPressureDao = bloodPressureDao, facility = facilityA))
        .test()

    // then
    val expected = PatientSearchResults(
        visitedCurrentFacility = listOf(patient1InFacilityA, patient3InFacilityA, patient2InFacilityA),
        notVisitedCurrentFacility = listOf(patient2InFacilityC, patient1InFacilityB, patient1InFacilityC)
    )
    testObserver.assertValue(expected)
  }
}
