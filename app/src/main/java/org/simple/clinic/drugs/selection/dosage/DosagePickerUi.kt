package org.simple.clinic.drugs.selection.dosage

interface DosagePickerUi {
  fun populateDosageList(list: List<DosageListItem>)

  // Not yet migrated to Mobius
  fun close()
}
