package org.simple.clinic.monthlyreports.form.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import org.simple.clinic.R

@Composable
fun LineSeparator() {
  Divider(
      color = colorResource(id = R.color.color_on_surface_11),
      modifier = Modifier
          .padding(
              top = dimensionResource(id = R.dimen.spacing_64),
              bottom = dimensionResource(id = R.dimen.spacing_40)
          )
          .fillMaxWidth()
          .height(1.dp)
  )
}
