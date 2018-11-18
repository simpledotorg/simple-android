package org.simple.clinic.overdue

import android.support.test.runner.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.simple.clinic.TestClinicApp
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class AppointmentAndroidTest {

  @Inject
  lateinit var configProvider: Single<AppointmentConfig>

  @Inject
  lateinit var moshi: Moshi

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun should_confirm_json_schema_once_v2_APIs_are_deployed() {
    if (configProvider.blockingGet().v2ApiEnabled.not()) {
      return
    }

    val reasonAdapter = moshi.adapter(Appointment.CancelReason::class.java)
    val reasonJson = reasonAdapter.toJson(Appointment.CancelReason.PHONE_DOES_NOT_WORK)
    assertThat(reasonJson).isNotEqualTo("phone_does_not_work")
  }
}
