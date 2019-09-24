package org.simple.clinic.illustration

import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.storage.files.FileStorage
import org.simple.clinic.storage.files.GetFileResult
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toOptional
import org.threeten.bp.LocalDate
import java.io.File
import javax.inject.Inject

class HomescreenIllustrationRepository @Inject constructor(
    private val illustrations: List<HomescreenIllustration>,
    private val fileStorage: FileStorage,
    private val userClock: UserClock
) {

  fun illustrationImageToShow(): Observable<File> =
      Observable.just(illustrations)
          .map { pickIllustration(it) }
          .ofType<Just<HomescreenIllustration>>()
          .map { it.value }
          .map { fileStorage.getFile(it.eventId) }
          .ofType<GetFileResult.Success>()
          .map { it.file }

  private fun pickIllustration(illustrations: List<HomescreenIllustration>): Optional<HomescreenIllustration> {
    val today = LocalDate.now(userClock)
    return illustrations
        .firstOrNull { illustration ->
          val showIllustrationFrom = toLocalDate(illustration.from)
          val showIllustrationTill = toLocalDate(illustration.to)

          today in showIllustrationFrom..showIllustrationTill
        }
        .toOptional()
  }

  private fun toLocalDate(dayOfMonth: DayOfMonth): LocalDate =
      LocalDate.now(userClock)
          .withMonth(dayOfMonth.month.value)
          .withDayOfMonth(dayOfMonth.day)
}
