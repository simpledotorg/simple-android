package org.simple.clinic.drugs.search

sealed class DrugSearchEffect

data class SearchDrugs(val searchQuery: String) : DrugSearchEffect()
