package org.simple.clinic.summary.clinicaldecisionsupport.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.common.ui.theme.SimpleTheme

@Composable
fun ClinicalDecisionHighBpAlert(
    showAlert: Boolean,
    animateExit: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = showAlert,
        enter = expandVertically(
            animationSpec = tween(500),
            expandFrom = Alignment.Top
        ),
        exit = if (animateExit) shrinkVertically(animationSpec = tween(500)) else ExitTransition.None
    ) {
        Card(
            modifier = modifier,
            elevation = dimensionResource(R.dimen.spacing_0),
            backgroundColor = colorResource(R.color.simple_red_100_alpha_50),
            border = BorderStroke(
                width = dimensionResource(id = R.dimen.spacing_1),
                color = colorResource(id = R.color.simple_red_600)
            ),

            ) {
            Row(
                modifier = Modifier.padding(dimensionResource(id = R.dimen.spacing_8)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.spacing_8))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_clinical_decision_warning),
                    contentDescription = null,
                )
                Column {
                    Text(
                        text = stringResource(id = R.string.clinical_decision_support_alert_bp_high_title),
                        style = SimpleTheme.typography.subtitle1Medium,
                        color = colorResource(id = R.color.simple_red_600)
                    )
                    Text(
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_2)),
                        text = stringResource(id = R.string.clinical_decision_support_alert_bp_high_subtitle),
                        style = MaterialTheme.typography.body1,
                        color = colorResource(id = R.color.simple_red_600_alpha_80)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClinicalDecisionHighBpAlertPreview() {
    SimpleTheme {
        ClinicalDecisionHighBpAlert(showAlert = true, animateExit = false)
    }
}
