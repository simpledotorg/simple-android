package org.simple.clinic.phone

import androidx.appcompat.app.AppCompatActivity
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.util.RxErrorsRule

class PhoneDialerTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private lateinit var phoneCaller: PhoneCaller
  private val dialer: Dialer = mock()
  private val activity: AppCompatActivity = mock()

  @Before
  fun setUp() {
    phoneCaller = PhoneCaller(activity)
  }

  @Test
  fun `when a normal call is made, the phone call should be made to the given number`() {
    val plainNumber = "123"

    phoneCaller.normalCall(plainNumber, dialer = dialer)

    verify(dialer).call(activity, plainNumber)
  }

  @Test
  fun `when a secure call is made, the phone call should be made to the masked number`() {
    val plainNumber = "123"

    phoneCaller.secureCall("987", plainNumber, dialer = dialer)

    verify(dialer).call(activity, "987,123#")
  }
}
