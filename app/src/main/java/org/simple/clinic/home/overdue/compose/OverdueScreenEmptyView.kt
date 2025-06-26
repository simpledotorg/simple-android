package org.simple.clinic.home.overdue.compose

import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme

@Composable
fun OverdueScreenEmptyView(
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AndroidView(
            factory = { ctx ->
                AppCompatImageView(ctx).apply {
                    setImageResource(R.drawable.illustration_overdue)
                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.FIT_START
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.spacing_32))
        )
        Text(
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.spacing_32)),
            text = stringResource(R.string.overdue_no_patients),
            style = SimpleTheme.typography.material.h6,
            color = SimpleTheme.colors.onSurface67
        )
        Text(
            modifier = Modifier.padding(
                start = dimensionResource(R.dimen.spacing_32),
                end = dimensionResource(R.dimen.spacing_32),
                top = dimensionResource(R.dimen.spacing_12)
            ),
            text = stringResource(R.string.overdue_empty_desc),
            textAlign = TextAlign.Center,
            style = SimpleTheme.typography.material.subtitle1,
            color = SimpleTheme.colors.onSurface67
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OverdueScreenEmptyPreview() {
    SimpleTheme {
        OverdueScreenEmptyView()
    }
}
