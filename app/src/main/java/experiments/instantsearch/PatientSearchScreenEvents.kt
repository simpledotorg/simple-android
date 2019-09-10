package experiments.instantsearch

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class SearchQueryTextChanged(val text: String) : UiEvent

class SearchClicked : UiEvent

data class PatientItemClicked(val patientUuid: UUID) : UiEvent
