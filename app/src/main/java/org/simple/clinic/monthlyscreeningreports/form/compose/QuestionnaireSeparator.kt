package org.simple.clinic.monthlyscreeningreports.form.compose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import org.simple.clinic.R

@Composable
fun Separator() {
  Spacer(
      Modifier.size(
          width = 0.dp,
          height = dimensionResource(id = R.dimen.spacing_40)
      )
  )
}

