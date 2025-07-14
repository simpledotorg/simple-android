package org.simple.clinic.summary.nextappointment.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import org.simple.clinic.R
import org.simple.clinic.common.ui.components.ButtonSize
import org.simple.clinic.common.ui.components.TextButton
import org.simple.clinic.common.ui.theme.SimpleTheme
import org.simple.clinic.summary.nextappointment.NextAppointmentState
import org.simple.clinic.util.Unicode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NextAppointment(
    modifier: Modifier = Modifier,
    state: NextAppointmentState,
    dateTimeFormatter: DateTimeFormatter,
    facilityName: String? = null,
    actionButtonText: String,
    onAction: () -> Unit,
) {
    Card(
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.surface
    )
    {
        Column(
            modifier = Modifier.padding(
                start = dimensionResource(R.dimen.spacing_16),
                bottom = dimensionResource(R.dimen.spacing_12)
            )
        ) {

            Row(
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.spacing_4),
                    end = dimensionResource(R.dimen.spacing_8)
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_8))
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.next_appointment_view_title),
                    style = SimpleTheme.typography.subtitle1Medium,
                    color = MaterialTheme.colors.onSurface,
                )

                TextButton(
                    buttonSize = ButtonSize.ExtraSmall,
                    onClick = onAction
                ) {
                    Text(
                        text = actionButtonText.uppercase(),
                        style = MaterialTheme.typography.button,
                    )
                }
            }

            val annotatedAppointmentDate = buildNextAppointmentAnnotatedString(
                state = state,
                dateTimeFormatter = dateTimeFormatter
            )
            Text(
                modifier = Modifier
                    .offset(y = (-dimensionResource(R.dimen.spacing_4)))
                    .padding(end = dimensionResource(R.dimen.spacing_16)),
                text = annotatedAppointmentDate,
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface,
            )

            if (!facilityName.isNullOrEmpty()) {
                Text(
                    modifier = Modifier
                        .padding(
                            top = dimensionResource(R.dimen.spacing_4),
                            bottom = dimensionResource(R.dimen.spacing_4),
                            end = dimensionResource(R.dimen.spacing_16)
                        ),
                    text = facilityName,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface,
                )
            }
        }
    }
}

@Composable
private fun buildNextAppointmentAnnotatedString(
    dateTimeFormatter: DateTimeFormatter,
    state: NextAppointmentState
): AnnotatedString {
    return buildAnnotatedString {
        when (state) {
            is NextAppointmentState.NoAppointment -> {
                withStyle(style = SpanStyle(color = colorResource(R.color.color_on_surface_67))) {
                    append(stringResource(R.string.next_appointment_none))
                }
            }

            is NextAppointmentState.Today -> {
                withStyle(style = SpanStyle(color = MaterialTheme.colors.onSurface)) {
                    append(dateTimeFormatter.format(state.date))
                }
                append("${Unicode.nonBreakingSpace}${Unicode.nonBreakingSpace}")
                withStyle(style = SpanStyle(color = colorResource(R.color.simple_green_500))) {
                    append(stringResource(R.string.next_appointment_today))
                }
            }

            is NextAppointmentState.Scheduled -> {
                withStyle(style = SpanStyle(color = MaterialTheme.colors.onSurface)) {
                    append(dateTimeFormatter.format(state.date))
                }
                append("${Unicode.nonBreakingSpace}${Unicode.nonBreakingSpace}")
                withStyle(style = SpanStyle(color = colorResource(R.color.simple_green_500))) {
                    append(
                        pluralStringResource(
                            R.plurals.next_appointment_plurals,
                            state.daysRemaining,
                            state.daysRemaining
                        )
                    )
                }
            }

            is NextAppointmentState.Overdue -> {
                withStyle(style = SpanStyle(color = MaterialTheme.colors.onSurface)) {
                    append(dateTimeFormatter.format(state.date))
                }
                append("${Unicode.nonBreakingSpace}${Unicode.nonBreakingSpace}")
                withStyle(style = SpanStyle(color = MaterialTheme.colors.error)) {
                    append(
                        pluralStringResource(
                            R.plurals.next_appointment_overdue_plurals,
                            state.overdueDays,
                            state.overdueDays
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun NextAppointmentTodayPreview() {
    val dateTimeFormatter = remember {
        DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH)
    }

    SimpleTheme {
        NextAppointment(
            state = NextAppointmentState.Today(LocalDate.now()),
            dateTimeFormatter = dateTimeFormatter,
            facilityName = "UHC Khardi",
            actionButtonText = "Change",
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun NextAppointmentScheduledPreview() {
    val dateTimeFormatter = remember {
        DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH)
    }

    SimpleTheme {
        NextAppointment(
            state = NextAppointmentState.Scheduled(LocalDate.now().plusDays(10), 10),
            dateTimeFormatter = dateTimeFormatter,
            actionButtonText = "Change",
            facilityName = "UHC Khardi",
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun NextAppointmentOverduePreview() {
    val dateTimeFormatter = remember {
        DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH)
    }

    SimpleTheme {
        NextAppointment(
            state = NextAppointmentState.Overdue(LocalDate.now().minusDays(5), 5),
            dateTimeFormatter = dateTimeFormatter,
            actionButtonText = "Change",
            facilityName = null,
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun NoAppointmentPreview() {
    val dateTimeFormatter = remember {
        DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH)
    }

    SimpleTheme {
        NextAppointment(
            state = NextAppointmentState.NoAppointment,
            dateTimeFormatter = dateTimeFormatter,
            actionButtonText = "Add",
            facilityName = null,
            onAction = {}
        )
    }
}
