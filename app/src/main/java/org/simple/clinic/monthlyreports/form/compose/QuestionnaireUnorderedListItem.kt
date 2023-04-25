package org.simple.clinic.monthlyreports.form.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.simple.clinic.R
import org.simple.clinic.monthlyreports.form.compose.util.getColor
import org.simple.clinic.questionnaire.component.UnorderedListItemComponentData
import java.util.Locale

@Composable
fun UnorderedListItem(
    unorderedListItemComponentData: UnorderedListItemComponentData
) {
  Row(
      modifier = Modifier
          .padding(top = dimensionResource(id = R.dimen.spacing_8)),
      verticalAlignment = Alignment.CenterVertically
  ) {
    IconByName(unorderedListItemComponentData)
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

@Composable
fun IconByName(
    unorderedListItemComponentData: UnorderedListItemComponentData
) {
  val name = unorderedListItemComponentData.icon
  val icon: ImageVector? = remember(name) {
    val formattedName = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    try {
      val cl = Class.forName("androidx.compose.material.icons.filled.${formattedName}Kt")
      val method = cl.declaredMethods.first()
      method.invoke(null, Icons.Filled) as ImageVector
    } catch (_: Throwable) {
      null
    }
  }
  if (icon != null) {
    Icon(
        icon,
        "$name icon",
        Modifier.size(16.dp),
        tint = getColor(unorderedListItemComponentData.iconColor)
    )
  }
}
