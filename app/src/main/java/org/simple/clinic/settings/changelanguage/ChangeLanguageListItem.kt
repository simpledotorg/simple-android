package org.simple.clinic.settings.changelanguage

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.list_change_language_view.*
import org.simple.clinic.R
import org.simple.clinic.settings.Language
import org.simple.clinic.settings.ProvidedLanguage
import org.simple.clinic.widgets.ItemAdapter
import org.simple.clinic.widgets.recyclerview.ViewHolderX

data class ChangeLanguageListItem(
    private val language: ProvidedLanguage,
    private val isSelected: Boolean
) : ItemAdapter.Item<ChangeLanguageListItem.Event> {

  companion object {
    fun from(languages: List<Language>, selectedLanguage: Language?): List<ChangeLanguageListItem> {
      return languages
          .filterIsInstance<ProvidedLanguage>()
          .map { language ->
            val isLanguageSelectedByUser = language == selectedLanguage

            ChangeLanguageListItem(language, isSelected = isLanguageSelectedByUser)
          }
    }
  }

  override fun layoutResId(): Int {
    return R.layout.list_change_language_view
  }

  override fun render(holder: ViewHolderX, subject: Subject<Event>) {
    holder.languageButton.apply {
      text = language.displayName
      isChecked = isSelected
      setOnClickListener { subject.onNext(Event.ListItemClicked(language)) }
    }
  }

  sealed class Event {
    data class ListItemClicked(val language: Language) : Event()
  }

  class DiffCallback : DiffUtil.ItemCallback<ChangeLanguageListItem>() {
    override fun areItemsTheSame(oldItem: ChangeLanguageListItem, newItem: ChangeLanguageListItem): Boolean {
      return oldItem.language.languageCode == newItem.language.languageCode
    }

    override fun areContentsTheSame(oldItem: ChangeLanguageListItem, newItem: ChangeLanguageListItem): Boolean {
      return oldItem == newItem
    }
  }
}
