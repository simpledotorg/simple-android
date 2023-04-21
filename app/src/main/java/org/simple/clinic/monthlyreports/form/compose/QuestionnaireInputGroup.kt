package org.simple.clinic.monthlyreports.form.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import org.simple.clinic.R
import org.simple.clinic.questionnaire.component.InputViewGroupComponentData

@Composable
fun InputGroup(
    inputViewGroupComponentData: InputViewGroupComponentData,
    content: MutableMap<String, Any?>
) {
  val inputFields = inputViewGroupComponentData.children
  if (!inputFields.isNullOrEmpty()) {
    LazyVerticalGrid(
        modifier = Modifier
            .padding(top = dimensionResource(id = R.dimen.spacing_8))
            .height(68.dp),
        columns = GridCells.Fixed(
            if (inputFields.count() > 1) 2 else 1
        ),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_8))
    ) {
      items(inputFields) { inputFieldComponentData ->
        InputField(inputFieldComponentData, content)
      }
    }
  }
}
