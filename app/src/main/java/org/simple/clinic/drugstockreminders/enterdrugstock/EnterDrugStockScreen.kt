package org.simple.clinic.drugstockreminders.enterdrugstock

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.spotify.mobius.functions.Consumer
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
    EnterDrugStockEvent,
    EnterDrugStockEffect,
    Unit>(), EnterDrugStockUi {

  @Inject
  lateinit var router: Router

  @Inject
  lateinit var effectHandler: EnterDrugStockEffectHandler

  private val toolbar
    get() = binding.toolbar

  private val webView
    get() = binding.webView

  override fun defaultModel() = EnterDrugStockModel.create()

  override fun uiRenderer() = EnterDrugStockScreenUiRenderer(this)

  override fun createUpdate() = EnterDrugStockUpdate()

  override fun createEffectHandler(viewEffectsConsumer: Consumer<Unit>) = effectHandler.build()

  override fun createInit() = EnterDrugStockInit()

  override fun bindView(layoutInflater: LayoutInflater, container: ViewGroup?) = ScreenEnterDrugStockBinding
      .inflate(layoutInflater, container, false)

  override fun onAttach(context: Context) {
    super.onAttach(context)
    context.injector<Injector>().inject(this)
  }

  @SuppressLint("SetJavaScriptEnabled")
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar.setNavigationOnClickListener { router.pop() }

    webView.settings.javaScriptEnabled = true
    webView.webViewClient = EnterDrugStockWebViewClient(
        backClicked = { router.pop() }
    )
  }

  override fun loadDrugStockForm(drugStockFormUrl: String) {
    webView.loadUrl(drugStockFormUrl)
  }

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
