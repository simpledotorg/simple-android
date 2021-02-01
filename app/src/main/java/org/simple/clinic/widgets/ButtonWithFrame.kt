package org.simple.clinic.widgets

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme

@Composable
fun ButtonWithFrame(
    text: String,
    @DrawableRes iconRes: Int? = null,
    onClick: () -> Unit
) {
  Surface(
      modifier = Modifier.fillMaxWidth(),
      color = MaterialTheme.colors.primaryVariant,
  ) {
    Button(modifier = Modifier.padding(8.dp), onClick = onClick) {
      if (iconRes != null) {
        Image(bitmap = imageResource(id = iconRes), contentDescription = null)
      }

      Text(text = text, style = MaterialTheme.typography.button)
    }
  }
}

@Preview(showBackground = true)
@Composable
fun ButtonWithFramePreview() {
  MdcTheme {
    ButtonWithFrame(text = "DONE") {
      /* Handle Click */
    }
  }
}
