package org.simple.clinic.reassignpatient

interface ReassignPatientUiActions {
  fun closeSheet(sheetClosedFrom: ReassignPatientSheetClosedFrom)
  fun openSelectFacilitySheet()
}
