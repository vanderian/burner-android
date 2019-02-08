package com.vander.burner.app.ui.screen

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import com.vander.burner.R
import com.vander.scaffold.screen.Empty
import com.vander.scaffold.screen.Screen
import kotlinx.android.synthetic.main.screen_init.*
import java.util.concurrent.TimeUnit

class InitScreen : Screen<Empty, Empty>() {
  override fun layout(): Int = R.layout.screen_init

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    ObjectAnimator.ofInt(progress, "progress", 0, 100)
        .setDuration(TimeUnit.SECONDS.toMillis(5))
        .start()
  }

  override fun intents(): Empty = Empty
  override fun render(state: Empty) {

  }
}