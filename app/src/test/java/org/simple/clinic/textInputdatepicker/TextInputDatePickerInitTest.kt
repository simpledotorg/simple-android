package org.simple.clinic.textInputdatepicker

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.textInputdatepicker.TextInputDatePickerEffect.PrefilledDate
import java.time.LocalDate

class TextInputDatePickerInitTest {

  @Test
  fun `when prefilled date is received, then show the date prefilled`() {
    val date = LocalDate.parse("2019-04-14")
    val minDate = LocalDate.parse("2019-04-04")
    val maxDate = LocalDate.parse("2020-04-04")
    val model = TextInputDatePickerModel.create(minDate, maxDate, prefilledDate = date)
    InitSpec(TextInputDatePickerInit())
        .whenInit(model)
        .then(
            assertThatFirst(
                hasModel(model),
                hasEffects(PrefilledDate(date))
            )
        )
  }
}
