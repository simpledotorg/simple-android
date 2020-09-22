package org.simple.clinic.signature

import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.TestClinicApp
import javax.inject.Inject

class SignatureRepositoryAndroidTest {

  @Inject
  lateinit var signatureRepository: SignatureRepository

  @Before
  fun setup() {
    TestClinicApp.appComponent().inject(this)
  }

  @After
  fun tearDown() {
    signatureRepository.deleteBitmap()
  }

  @Test
  fun saving_a_signature_should_work_correctly() {
    // given
    val widthPx = 500
    val heightPx = 500
    val bitmapConfig = Bitmap.Config.ARGB_8888
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, bitmapConfig)

    assertThat(signatureRepository.getSignatureBitmap()).isNull()

    // when
    signatureRepository.saveSignatureBitmap(bitmap)

    // then
    assertThat(signatureRepository.getSignatureBitmap()).isNotNull()
  }
}
