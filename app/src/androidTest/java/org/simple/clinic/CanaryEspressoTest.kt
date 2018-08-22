package org.simple.clinic

import android.content.SharedPreferences
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.activity.TheActivity
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class CanaryEspressoTest {

  @Rule
  @JvmField
  var activityRule: ActivityTestRule<TheActivity> = ActivityTestRule(TheActivity::class.java, false, false /* launchActivity */)

  @Inject
  lateinit var sharedPreferences: SharedPreferences

  @Inject
  lateinit var appDatabase: AppDatabase

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
    sharedPreferences.edit().clear().commit()
    appDatabase.clearAllTables()

    activityRule.launchActivity(null)
  }

  @Test
  fun app_starts_without_crashing() {
    onView(withId(R.id.onboarding_logo_container)).check(matches(isDisplayed()))
  }
}
