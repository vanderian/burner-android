package com.vander.burner.app.ui.screen

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding3.view.clicks
import com.vander.burner.R
import com.vander.burner.app.ui.animatedClose
import com.vander.scaffold.form.FormInput
import com.vander.scaffold.screen.Screen
import com.vander.scaffold.ui.setText
import com.vander.scaffold.ui.visibility
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_appbar.*
import kotlinx.android.synthetic.main.screen_pair.*

class PairScreen : Screen<PairState, PairIntents>() {
  private lateinit var form: FormInput

  override fun layout(): Int = R.layout.screen_pair

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar.setTitle(R.string.title_pair)
    toolbar.animatedClose()
    form = FormInput().withTextInputs(inputAddress)
  }

  override fun intents(): PairIntents = object : PairIntents {
    override val form: FormInput
      get() = this@PairScreen.form

    override val toolbar: Toolbar
      get() = this@PairScreen.toolbar

    override fun pair(): Observable<Unit> = buttonPair.clicks()
    override fun scan(): Observable<Unit> = fab.clicks()
    override fun clear(): Observable<Unit> = buttonClear.clicks()

    override fun events(): List<Observable<*>> = listOf(
        event(TransferData::class).doOnNext { inputAddress.setText(it.address) }
    ) + form.events(this@PairScreen)
  }

  override fun render(state: PairState) {
    inputAddress.setText(state.address)
    buttonClear.visibility = state.address.isNotEmpty().visibility()
  }

}
