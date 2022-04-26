package org.simple.clinic.drugstockreminders.enterdrugstock

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.parcelize.Parcelize
import org.simple.clinic.databinding.ScreenEnterDrugStockBinding
import org.simple.clinic.di.injector
import org.simple.clinic.navigation.v2.Router
import org.simple.clinic.navigation.v2.ScreenKey
import org.simple.clinic.navigation.v2.fragments.BaseScreen
import javax.inject.Inject

class EnterDrugStockScreen : BaseScreen<
    EnterDrugStockScreen.Key,
    ScreenEnterDrugStockBinding,
    EnterDrugStockModel,
    Unit,
    Unit,
    Unit>() {

  @Inject
  lateinit var router: Router

  private val toolbar
    get() = binding.toolbar

  private val webView
    get() = binding.webView

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  @SuppressLint("SetJavaScriptEnabled")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar.setNavigationOnClickListener { router.pop() }

    webView.settings.javaScriptEnabled = true
  }

  override fun defaultModel() = EnterDrugStockModel

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) = ScreenEnterDrugStockBinding
      .inflate(layoutInflater, container, false)

  override fun uiRenderer() = EnterDrugStockScreenUiRenderer()

  interface Injector {
    fun inject(target: EnterDrugStockScreen)
  }

  @Parcelize
  data class Key(
      override val analyticsName: String = "Enter Drug Stock Report"
  ) : ScreenKey() {

    override fun instantiateFragment() = EnterDrugStockScreen()
  }
}
