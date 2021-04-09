package org.simple.clinic.storage.monitoring

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DbMethodLookupTest {

  private val csvMetadata = """
    |PrescribedDrugRoomDao_Impl,save,229,239
    |PrescribedDrugRoomDao_Impl,updateSyncStatus,241,269
    |PrescribedDrugRoomDao_Impl,softDelete,271,304
    |PrescribedDrugRoomDao_Impl,clearData,306,319
    |PrescribedDrugRoomDao_Impl,purgeDeleted,321,333
    |PrescribedDrugRoomDao_Impl,updateDrugDuration,335,374
    |PrescribedDrugRoomDao_Impl,updateDrugFrequenecy,376,421
    |PrescribedDrugRoomDao_Impl,deleteWithoutLinkedPatient,423,435
    |PrescribedDrugRoomDao_Impl,withSyncStatus,437,540
    |PrescribedDrugRoomDao_Impl,getOne,542,645
    |PrescribedDrugRoomDao_Impl,recordIdsWithSyncStatus,647,675
    |PrescribedDrugRoomDao_Impl,count,677,709
    |PrescribedDrugRoomDao_Impl,forPatient,753,864
    |PrescribedDrugRoomDao_Impl,forPatientImmediate,866,969
    |PrescribedDrugRoomDao_Impl,prescription,971,1082
    |PrescribedDrugRoomDao_Impl,prescriptionImmediate,1084,1187
    |PrescribedDrugRoomDao_Impl,hasPrescriptionForPatientChangedSince,1189,1243
    |PrescribedDrugRoomDao_Impl,getAllPrescribedDrugs,1245,1340
    |PrescribedDrugRoomDao_Impl,addTeleconsultationIdToDrugs,1382,1441
    |BloodSugarMeasurementRoomDao_Impl,save,197,207
    |BloodSugarMeasurementRoomDao_Impl,updateSyncStatus,209,237
    |BloodSugarMeasurementRoomDao_Impl,clear,239,251
    |BloodSugarMeasurementRoomDao_Impl,purgeDeleted,253,265
    |BloodSugarMeasurementRoomDao_Impl,deleteWithoutLinkedPatient,267,279
    |BloodSugarMeasurementRoomDao_Impl,latestMeasurements,281,387
    |BloodSugarMeasurementRoomDao_Impl,latestMeasurementsImmediate,389,487
    |BloodSugarMeasurementRoomDao_Impl,allBloodSugars,489,592
    |BloodSugarMeasurementRoomDao_Impl,allBloodSugarsDataSource,594,693
    |BloodSugarMeasurementRoomDao_Impl,withSyncStatus,695,786
    |BloodSugarMeasurementRoomDao_Impl,getOne,788,879
    |BloodSugarMeasurementRoomDao_Impl,recordIdsWithSyncStatus,881,909
    |BloodSugarMeasurementRoomDao_Impl,count,911,943
    |BloodSugarMeasurementRoomDao_Impl,recordedBloodSugarsCountForPatient,987,1031
    |BloodSugarMeasurementRoomDao_Impl,recordedBloodSugarsCountForPatientImmediate,1033,1063
    |BloodSugarMeasurementRoomDao_Impl,haveBloodSugarsForPatientChangedSince,1065,1119
    |BloodSugarMeasurementRoomDao_Impl,getAllBloodSugarMeasurements,1121,1204
  """.trimMargin()

  private val dbMethodLookup = DbMethodLookup(csvMetadata)

  @Test
  fun `looking up a method which is present in the metadata with a valid line number should work`() {
    // when
    val method = dbMethodLookup.find("PrescribedDrugRoomDao_Impl", 972)

    // then
    assertThat(method).isEqualTo("prescription")
  }

  @Test
  fun `looking up a method for a line number that does not match a dao should return null`() {
    // when
    val method = dbMethodLookup.find("BloodSugarMeasurementRoomDao_Impl", 1120)

    // then
    assertThat(method).isNull()
  }

  @Test
  fun `looking up a method for a dao whose metadata is not present should return null`() {
    // when
    val method = dbMethodLookup.find("OverdueAppointmentRoomDao_Impl", 650)

    // then
    assertThat(method).isNull()
  }
}
