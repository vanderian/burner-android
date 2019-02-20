package com.vander.burner.app.ui.misc

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.vander.burner.R
import com.vander.scaffold.ui.gone
import com.vander.scaffold.ui.visible

class LoadingLayout @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
  : LinearLayout(context, attrs, defStyleAttr) {
  private var startTime: Long = -1
  private var postedHide = false
  private var postedShow = false
  private var dismissed = false

  private val delayedHide = Runnable {
    postedHide = false
    startTime = -1
    gone()
  }

  private val delayedShow = Runnable {
    postedShow = false
    if (!dismissed) {
      startTime = System.currentTimeMillis()
      visible()
    }
  }

  init {
    LayoutInflater.from(context).inflate(R.layout.view_loading, this, true)
    setOnTouchListener { _, _ -> true } //consume touches
  }

  public override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    removeCallbacks()
  }

  public override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    removeCallbacks()
  }

  private fun removeCallbacks() {
    removeCallbacks(delayedHide)
    removeCallbacks(delayedShow)
  }

  /**
   * Hide the progress view if it is visible. The progress view will not be
   * hidden until it has been shown for at least a minimum show time. If the
   * progress view was not yet visible, cancels showing the progress view.
   */
  @Synchronized fun hide() {
    dismissed = true
    removeCallbacks(delayedShow)
    postedShow = false
    val diff = System.currentTimeMillis() - startTime
    if (diff >= MIN_SHOW_TIME || startTime == -1L) {
      // The progress spinner has been shown long enough
      // OR was not shown yet. If it wasn't shown yet,
      // it will just never be shown.
      gone()
    } else {
      // The progress spinner is shown, but not long enough,
      // so put a delayed message in to hide it when its been
      // shown long enough.
      if (!postedHide) {
        postDelayed(delayedHide, MIN_SHOW_TIME - diff)
        postedHide = true
      }
    }
  }

  /**
   * Show the progress view after waiting for a minimum delay. If
   * during that time, hide() is called, the view is never made visible.
   */
  @Synchronized fun show() {
    // Reset the start time.
    startTime = -1
    dismissed = false
    removeCallbacks(delayedHide)
    postedHide = false
    if (!postedShow) {
      postDelayed(delayedShow, MIN_DELAY.toLong())
      postedShow = true
    }
  }

  fun shouldShow(show: Boolean) {
    if (show) show() else hide()
  }

  companion object {
    private const val MIN_SHOW_TIME = 500 // ms
    private const val MIN_DELAY = 500 // ms
  }

}