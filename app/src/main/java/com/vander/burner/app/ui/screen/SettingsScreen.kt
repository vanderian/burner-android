package com.vander.burner.app.ui.screen

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding3.view.clicks
import com.vander.burner.BuildConfig
import com.vander.burner.R
import com.vander.burner.app.net.apiHandler
import com.vander.burner.app.ui.ShowDialogEvent
import com.vander.burner.app.ui.animatedClose
import com.vander.burner.app.ui.copyToClipboard
import com.vander.burner.app.ui.showConfirmDialog
import com.vander.scaffold.form.FormInput
import com.vander.scaffold.screen.Empty
import com.vander.scaffold.screen.Screen
import com.vander.scaffold.ui.isVisible
import com.vander.scaffold.ui.visibility
import io.reactivex.Maybe
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_appbar.*
import kotlinx.android.synthetic.main.screen_settings.*

class SettingsScreen : Screen<Empty, SettingsIntents>() {
  private lateinit var form: FormInput

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar.setTitle(R.string.title_settings)
    toolbar.animatedClose()
    form = FormInput().withTextInputs(inputPrivate, inputSeed)
    textVersion.text = BuildConfig.VERSION_NAME
  }

  override fun layout(): Int = R.layout.screen_settings

  override fun intents(): SettingsIntents = object : SettingsIntents {
    override val toolbar: Toolbar
      get() = this@SettingsScreen.toolbar
    override val form: FormInput
      get() = this@SettingsScreen.form

    override fun burn(): Observable<Unit> = buttonBurn.clicks()
    override fun burnConfirm(ev: ShowDialogEvent): Maybe<Unit> = requireContext().showConfirmDialog(ev)
    override fun pair(): Observable<Unit> = buttonPair.clicks()
    override fun copy(): Observable<Unit> = buttonCopy.clicks()
    override fun show(): Observable<Unit> = buttonShow.clicks().doOnNext { imageQrPrivate.visibility = imageQrPrivate.isVisible().not().visibility() }
    override fun createFromKey(): Observable<Unit> = buttonCreatePrivate.clicks()
    override fun createFromSeed(): Observable<Unit> = buttonCreateSeed.clicks()
    override fun beer(): Observable<Unit> = buttonBeer.clicks()
    override fun focusChanges(): Observable<Pair<Int, Boolean>> = super.focusChanges().doOnNext { (id, hasFocus) ->
      buttonForInput(id).let { (input, btn) ->
        btn.visibility = hasFocus.visibility()
        form.validationEnabled(input, hasFocus)
      }
    }

    override fun events(): List<Observable<*>> = listOf(
        event(ClipboardEvent::class).doOnNext { requireContext().copyToClipboard("pk", it.text, R.string.label_copy_address) },
        event(ShowKeyEvent::class).doOnNext { if (imageQrPrivate.drawable == null) imageQrPrivate.setImageBitmap(it.bmp) }
    ) + form.events(this@SettingsScreen) + apiHandler { }
  }

  override fun render(state: Empty) {

  }

  private fun buttonForInput(id: Int) = when (id) {
    R.id.inputPrivate -> inputPrivate to buttonCreatePrivate
    R.id.inputSeed -> inputSeed to buttonCreateSeed
    else -> throw IllegalArgumentException()
  }
}
