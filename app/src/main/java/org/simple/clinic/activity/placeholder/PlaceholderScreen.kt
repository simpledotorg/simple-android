package org.simple.clinic.activity.placeholder

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.R
import org.simple.clinic.await.Await
import org.simple.clinic.await.Checkpoint
import org.simple.clinic.databinding.ScreenPlaceholderBinding
import org.simple.clinic.router.screen.FullScreenKey
import java.util.concurrent.TimeUnit.SECONDS

class PlaceholderScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
  private val delayToShowMessage = SECONDS.toMillis(3).toInt()
  private val await = Await(listOf(Checkpoint.unit(delayToShowMessage)))
  private var awaitDisposable: Disposable? = null
  private var binding: ScreenPlaceholderBinding? = null

  private val loadingTextLayout
    get() = binding!!.loadingTextLayout

  private val loadingProgressBar
    get() = binding!!.loadingProgressBar

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }

    binding = ScreenPlaceholderBinding.bind(this)
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    awaitDisposable = await.items()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { showLoadingUi() }
  }

  override fun onDetachedFromWindow() {
    awaitDisposable?.dispose()
    binding = null
    super.onDetachedFromWindow()
  }

  private fun showLoadingUi() {
    loadingTextLayout.visibility = VISIBLE
    loadingProgressBar.visibility = VISIBLE
  }

  @Parcelize
  object PlaceHolderScreenKey : FullScreenKey {

    override val analyticsName = "Placeholder Screen"

    override fun layoutRes() = R.layout.screen_placeholder
  }
}
