package org.simple.clinic.reassignPatient

interface ReassignPatientUiActions {
  fun closeSheet(sheetClosedFrom: ReassignPatientSheetClosedFrom)
  fun openSelectFacilitySheet()
}
