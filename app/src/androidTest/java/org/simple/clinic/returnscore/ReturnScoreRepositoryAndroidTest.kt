package org.simple.clinic.returnscore

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.simple.clinic.AppDatabase
import org.simple.clinic.TestClinicApp
import org.simple.clinic.TestData
import org.simple.clinic.rules.SaveDatabaseRule
import org.simple.clinic.util.Rules
import java.util.UUID
import javax.inject.Inject

class ReturnScoreRepositoryAndroidTest {

  @Inject
  lateinit var database: AppDatabase

  @Inject
  lateinit var returnScoreRepository: ReturnScoreRepository

  @get:Rule
  val rules: RuleChain = Rules
      .global()
      .around(SaveDatabaseRule())

  @Before
  fun setUp() {
    TestClinicApp.appComponent().inject(this)
  }

  @Test
  fun saving_return_scores_should_work_correctly() {
    // given
    val returnScores = listOf(
        TestData.returnScore(
            uuid = UUID.fromString("ef5b7656-a6df-459c-a5b0-80d100721597"),
        ),
        TestData.returnScore(
            uuid = UUID.fromString("ef5b7656-a6df-459c-a5b0-80d123021597"),
        )
    )

    // when
    returnScoreRepository.save(returnScores)

    // then
    val savedReturnScores = returnScoreRepository.returnScoresImmediate()

    assertThat(savedReturnScores).isEqualTo(returnScores)
  }
}
