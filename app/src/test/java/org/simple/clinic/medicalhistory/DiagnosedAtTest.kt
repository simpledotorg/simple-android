package org.simple.clinic.medicalhistory


import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.simple.clinic.medicalhistory.Answer.No
import org.simple.clinic.medicalhistory.Answer.Suspected
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.Answer.Yes
import java.time.Instant

class DiagnosedAtTest {
  private val now = Instant.parse("2025-11-25T10:15:30Z")
  private val existingTs = Instant.parse("2025-01-01T00:00:00Z")

  private fun call(
      existingAnswer: Answer?,
      newAnswer: Answer,
      existingTimestamp: Instant?,
      now: Instant
  ): Instant? {
    return diagnosedAt(existingAnswer, newAnswer, existingTimestamp, now)
  }

  @Test
  fun `when new answer is Suspected then returns null even if existing timestamp present`() {
    assertNull(call(existingAnswer = Yes, newAnswer = Suspected, existingTimestamp = existingTs, now = now))
    assertNull(call(existingAnswer = null, newAnswer = Suspected, existingTimestamp = existingTs, now = now))
  }

  @Test
  fun `when existing timestamp is present and new answer is definitive then preserve the existing timestamp`() {
    // If timestamp exists it should be returned (write-once).
    assertEquals(existingTs, call(existingAnswer = Yes, newAnswer = Yes, existingTimestamp = existingTs, now = now))
    assertEquals(existingTs, call(existingAnswer = Suspected, newAnswer = No, existingTimestamp = existingTs, now = now))
  }

  @Test
  fun `when existingAnswer is null and new answer is yes or no then return now`() {
    assertEquals(now, call(existingAnswer = null, newAnswer = Yes, existingTimestamp = null, now = now))
    assertEquals(now, call(existingAnswer = null, newAnswer = No, existingTimestamp = null, now = now))
  }

  @Test
  fun `when existingAnswer is null and new answer is Suspected or Unanswered then return null`() {
    assertNull(call(existingAnswer = null, newAnswer = Suspected, existingTimestamp = null, now = now))
    assertNull(call(existingAnswer = null, newAnswer = Unanswered, existingTimestamp = null, now = now))
  }

  @Test
  fun `when existingAnswer is suspected or unanswered and new answer is yes or no then return now`() {
    // Previously Suspected -> new definitive should stamp now
    assertEquals(now, call(existingAnswer = Suspected, newAnswer = Yes, existingTimestamp = null, now = now))
    assertEquals(now, call(existingAnswer = Unanswered, newAnswer = No, existingTimestamp = null, now = now))
  }

  @Test
  fun `when existingAnswer is yes or no with no timestamp and new answer is Yes or No then return null`() {
    assertNull(call(existingAnswer = Yes, newAnswer = No, existingTimestamp = null, now = now))
    assertNull(call(existingAnswer = No, newAnswer = Yes, existingTimestamp = null, now = now))
    assertNull(call(existingAnswer = Yes, newAnswer = Yes, existingTimestamp = null, now = now))
  }

  @Test
  fun `When existing timestamp is present then always preserve it except when suspected where return null`() {
    assertEquals(existingTs, call(existingAnswer = Yes, newAnswer = Yes, existingTimestamp = existingTs, now = now))
    assertNull(call(existingAnswer = Yes, newAnswer = Suspected, existingTimestamp = existingTs, now = now))
  }
}
