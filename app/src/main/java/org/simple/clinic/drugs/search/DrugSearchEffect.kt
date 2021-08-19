package org.simple.clinic.drugs.search

import androidx.paging.PagingData
import java.util.UUID

sealed class DrugSearchEffect

data class SearchDrugs(val searchQuery: String) : DrugSearchEffect()

data class SetDrugsSearchResults(val searchResults: PagingData<Drug>) : DrugSearchEffect()

data class OpenCustomDrugEntrySheetFromDrugList(
    val drugUuid: UUID,
    val patientUuid: UUID
) : DrugSearchEffect()
