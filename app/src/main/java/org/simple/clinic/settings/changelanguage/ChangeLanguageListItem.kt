package org.simple.clinic.settings.changelanguage

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.subjects.Subject
import org.simple.clinic.R
import org.simple.clinic.databinding.ListChangeLanguageViewBinding
import org.simple.clinic.settings.Language
import org.simple.clinic.settings.ProvidedLanguage
import org.simple.clinic.widgets.BindingItemAdapter
import org.simple.clinic.widgets.recyclerview.BindingViewHolder

data class ChangeLanguageListItem(
    private val language: ProvidedLanguage,
    private val isLanguageSelectedByUser: Boolean
) : BindingItemAdapter.Item<ChangeLanguageListItem.Event> {

  companion object {
    fun from(languages: List<Language>, selectedLanguage: Language?): List<ChangeLanguageListItem> {
      return languages
          .filterIsInstance<ProvidedLanguage>()
          .map { language ->
            val isLanguageSelectedByUser = language == selectedLanguage

            ChangeLanguageListItem(language, isLanguageSelectedByUser = isLanguageSelectedByUser)
          }
    }
  }

  override fun layoutResId(): Int {
    return R.layout.list_change_language_view
  }

  override fun render(holder: BindingViewHolder, subject: Subject<Event>) {
    val binding = holder.binding as ListChangeLanguageViewBinding

    binding.languageButton.apply {
      text = language.displayName
      isChecked = isLanguageSelectedByUser
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
