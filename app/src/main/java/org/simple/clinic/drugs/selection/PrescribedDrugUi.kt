package org.simple.clinic.drugs.selection

import com.xwray.groupie.ViewHolder
import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.summary.GroupieItemWithUiEvents
import java.util.UUID

interface PrescribedDrugUi : EditMedicinesUiActions {
  fun populateDrugsList(protocolDrugItems: List<GroupieItemWithUiEvents<out ViewHolder>>)
}
