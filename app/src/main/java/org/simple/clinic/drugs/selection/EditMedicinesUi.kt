package org.simple.clinic.drugs.selection

import com.xwray.groupie.ViewHolder
import org.simple.clinic.summary.GroupieItemWithUiEvents

interface EditMedicinesUi {
  fun populateDrugsList(protocolDrugItems: List<GroupieItemWithUiEvents<out ViewHolder>>)
  fun showDoneButton()
  fun hideRefillMedicineButton()
}
