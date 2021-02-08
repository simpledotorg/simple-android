package org.simple.clinic.drugs.selection

import com.xwray.groupie.GroupieViewHolder
import org.simple.clinic.summary.GroupieItemWithUiEvents

interface EditMedicinesUi {
  fun populateDrugsList(protocolDrugItems: List<GroupieItemWithUiEvents<out GroupieViewHolder>>)
  fun showDoneButton()
  fun hideRefillMedicineButton()
  fun showRefillMedicineButton()
  fun hideDoneButton()
}
