package org.simple.clinic.drugs.search

import androidx.paging.PagingData
import java.util.UUID

interface UiActions {
  fun setDrugSearchResults(searchResults: PagingData<Drug>)
  fun openCustomDrugEntrySheetFromDrugList(drugUuid: UUID, patientUuid: UUID)
}
