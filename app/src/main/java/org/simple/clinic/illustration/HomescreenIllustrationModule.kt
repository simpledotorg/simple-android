package org.simple.clinic.illustration

import dagger.Module
import dagger.Provides
import org.threeten.bp.Month
import retrofit2.Retrofit
import javax.inject.Named

@Module
class HomescreenIllustrationModule {

  @Provides
  @Named("homescreen-illustration-folder")
  fun illustrationsFolder() = "homescreen-illustrations"

  @Provides
  fun fileDownloadService(retrofit: Retrofit): FileDownloadService = retrofit.create(FileDownloadService::class.java)

  @Provides
  fun illustrations(): List<HomescreenIllustration> =
      listOf(
          HomescreenIllustration(
              eventId = "valmiki-jayanti.png",
              illustrationUrl = "https://firebasestorage.googleapis.com/v0/b/simple-org.appspot.com/o/valmiki.png?alt=media&token=15a1f9da-3712-403b-aa13-e51cba37ef88",
              from = DayOfMonth(20, Month.SEPTEMBER),
              to = DayOfMonth(30, Month.SEPTEMBER)
          )
      )
}
