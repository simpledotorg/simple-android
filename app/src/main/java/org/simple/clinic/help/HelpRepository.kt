package org.simple.clinic.help

import androidx.annotation.VisibleForTesting
import io.reactivex.Observable
import org.simple.clinic.di.AppScope
import org.simple.clinic.storage.text.TextStore
import org.simple.clinic.util.Optional
import javax.inject.Inject

@AppScope
class HelpRepository @Inject constructor(
    private val textStore: TextStore
) {

  companion object {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    const val HELP_KEY = "help"
  }

  fun helpContentText(): Observable<Optional<String>> {
    return textStore.getAsStream(HELP_KEY)
  }

  fun updateHelp(helpContent: String) {
    textStore.put(HELP_KEY, helpContent)
  }
}
