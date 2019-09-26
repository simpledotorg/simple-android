package org.simple.clinic.illustration

import io.reactivex.Observable
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional
import org.simple.clinic.util.UserClock
import org.simple.clinic.util.toOptional
import org.threeten.bp.LocalDate
import java.io.File
import javax.inject.Inject
import javax.inject.Named

class HomescreenIllustrationRepository @Inject constructor(
    private val illustrations: List<HomescreenIllustration>,
    private val userClock: UserClock,
    @Named("homescreen-illustration-folder") private val illustrationsFolder: File
) {

  fun illustrationImageToShow(): Observable<File> =
      Observable.just(illustrations)
          .map { pickIllustration(it) }
          .ofType<Just<HomescreenIllustration>>()
          .map { it.value }
          .map { File(illustrationsFolder, it.eventId) }
          .filter { it.exists() }

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
