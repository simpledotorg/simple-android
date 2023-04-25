package org.simple.clinic.monthlyreports.form.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.simple.clinic.R
import org.simple.clinic.questionnaire.component.UnorderedListItemComponentData

@Composable
fun UnorderedListItem(
    unorderedListItemComponentData: UnorderedListItemComponentData
) {
  Row(
      modifier = Modifier
          .padding(top = dimensionResource(id = R.dimen.spacing_8)),
      verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
        Icons.Rounded.Check,
        unorderedListItemComponentData.icon,
        Modifier.size(16.dp),
    )
    Text(
        text = unorderedListItemComponentData.text,
        modifier = Modifier.padding(
            start = dimensionResource(id = R.dimen.spacing_12)),
        style = TextStyle(
            color = colorResource(id = R.color.simple_dark_grey),
            fontSize = 14.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight(400),
            lineHeight = 20.sp,
        )
    )
  }
}
