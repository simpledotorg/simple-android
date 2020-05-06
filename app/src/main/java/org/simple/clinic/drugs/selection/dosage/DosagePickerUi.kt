package org.simple.clinic.drugs.selection.dosage

interface DosagePickerUi : DosagePickerUiActions {
  fun populateDosageList(list: List<DosageListItem>)
}
