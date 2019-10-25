package org.simple.clinic.activity.placeholder

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.screen_placeholder.view.*
import org.simple.clinic.await.Await
import org.simple.clinic.await.Checkpoint
import java.util.concurrent.TimeUnit.SECONDS

class PlaceholderScreen(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {
  private val delayToShowMessage = SECONDS.toMillis(3).toInt()
  private val await = Await(listOf(Checkpoint.unit(delayToShowMessage)))
  private var awaitDisposable: Disposable? = null

  override fun onFinishInflate() {
    super.onFinishInflate()
    if (isInEditMode) {
      return
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    awaitDisposable = await.items()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { showLoadingUi() }
  }

  override fun onDetachedFromWindow() {
    awaitDisposable?.dispose()
    super.onDetachedFromWindow()
  }

  private fun showLoadingUi() {
    loadingTextLayout.visibility = VISIBLE
    loadingProgressBar.visibility = VISIBLE
  }
}
