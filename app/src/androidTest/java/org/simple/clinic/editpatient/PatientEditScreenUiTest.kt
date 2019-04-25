package org.simple.clinic.editpatient

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.R
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.activity.TheActivity
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.router.screen.RouterDirection
import org.threeten.bp.LocalDate
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class PatientEditScreenUiTest {

  @get:Rule
  val activityTestRule = ActivityTestRule(TheActivity::class.java)

  @Inject
  lateinit var testData: TestData

  @Inject
  lateinit var patientRepository: PatientRepository

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun when_opening_the_screen_the_patient_date_of_birth_should_be_prefilled_in_a_specific_format() {
    val patientProfile = testData.patientProfile(generateBusinessId = false)
        .let { profile ->
          profile.copy(patient = profile.patient.copy(age = null, dateOfBirth = LocalDate.parse("1990-05-25")))
        }

    patientRepository.save(listOf(patientProfile)).blockingAwait()

    activityTestRule.runOnUiThread {
      activityTestRule.activity.screenRouter.clearHistoryAndPush(PatientEditScreenKey(patientProfile.patient.uuid), RouterDirection.FORWARD)
    }

    onView(withId(R.id.patientedit_date_of_birth)).check(matches(withText("25/05/1990")))
  }
}
