package org.simple.clinic.illustration

import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.storage.files.FileStorage
import org.simple.clinic.storage.files.GetFileResult
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toOptional
import org.simple.clinic.util.toUtcInstant
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import javax.inject.Inject

class HomescreenIllustrationRepository @Inject constructor(
    private val illustrationDao: HomescreenIllustration.RoomDao,
    private val fileStorage: FileStorage,
    private val userClock: UserClock
) {

  fun illustrations(): Observable<GetFileResult.Success> =
      illustrationDao.illustrations()
          .map { pickIllustration(it) }
          .ofType<Just<HomescreenIllustration>>()
          .map { it.value }
          .map { fileStorage.getFile(it.eventId) }
          .ofType()

  private fun pickIllustration(illustrations: List<HomescreenIllustration>): Optional<HomescreenIllustration> {
    illustrations.forEach {
      if (userClock.instant() in toInstant(it.from)..toInstant(it.to)) {
        return it.toOptional()
      }
    }
    return None
  }

  private fun toInstant(dayOfMonth: DayOfMonth): Instant =
      LocalDate.now(userClock)
          .withMonth(dayOfMonth.month.value)
          .withDayOfMonth(dayOfMonth.day)
          .toUtcInstant(userClock)
}
