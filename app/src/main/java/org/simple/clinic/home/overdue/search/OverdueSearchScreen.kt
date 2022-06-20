package org.simple.clinic.home.overdue.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.parcelize.Parcelize
import org.simple.clinic.databinding.ScreenOverdueSearchBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import javax.inject.Inject

class OverdueSearchScreen : BaseScreen<
    OverdueSearchScreen.Key,
    ScreenOverdueSearchBinding,
    OverdueSearchModel,
    OverdueSearchEvent,
    OverdueSearchEffect,
    Nothing>() {

  @Inject
  lateinit var router: Router

  private val overdueSearchToolbar
    get() = binding.overdueSearchToolbar

  override fun defaultModel(): OverdueSearchModel {
    return OverdueSearchModel.create()
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?): ScreenOverdueSearchBinding {
    return ScreenOverdueSearchBinding.inflate(layoutInflater, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    overdueSearchToolbar.setNavigationOnClickListener {
      router.pop()
    }
  }

  interface Injector {
    fun inject(target: OverdueSearchScreen)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Overdue Search Screen"
  ) : ScreenKey() {

    override fun instantiateFragment(): Fragment {
      return OverdueSearchScreen()
    }
  }
}
