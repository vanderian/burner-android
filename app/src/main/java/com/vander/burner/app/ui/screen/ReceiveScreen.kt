package com.vander.burner.app.ui.screen

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding3.view.clicks
import com.vander.burner.R
import com.vander.burner.app.ui.animatedClose
import com.vander.burner.app.ui.copyToClipboard
import com.vander.burner.app.validator.DecimalDigitsInputFilter
import com.vander.scaffold.form.FormInput
import com.vander.scaffold.screen.Screen
import com.vander.scaffold.ui.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.layout_appbar.*
import kotlinx.android.synthetic.main.screen_receive.*

class ReceiveScreen : Screen<ReceiveState, ReceiveIntents>(), HandlesBack {
  private val back = PublishSubject.create<Unit>()
  lateinit var form: FormInput

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar.setTitle(R.string.title_receive)
    toolbar.animatedClose()
    form = FormInput().withTextInputs(inputAmount, inputMessage)
    inputAmount.editText!!.filters = arrayOf(DecimalDigitsInputFilter())
  }

  override fun layout(): Int = R.layout.screen_receive

  override fun intents(): ReceiveIntents = object : ReceiveIntents {
    override val form: FormInput
      get() = this@ReceiveScreen.form

    override val toolbar: Toolbar
      get() = this@ReceiveScreen.toolbar

    override fun copy(): Observable<Unit> = Observable.merge(
        buttonAddress.clicks().map { requireContext().copyToClipboard("public key", buttonAddress.text.toString(), R.string.result_copy_address) },
        textUrl.clicks().map { requireContext().copyToClipboard("request link", textUrl.text, R.string.result_copy_link) }
    ).map { Unit }

    override fun toggle(): Observable<Unit> = Observable.merge(
        buttonValues.clicks(),
        back.doAfterNext {
          inputAmount.setText("")
          inputMessage.setText("")
        })

    override fun events(): List<Observable<*>> = form.events(this@ReceiveScreen)
  }

  override fun render(state: ReceiveState) {
    state.qr?.let {
      imageQr.setImageBitmap(it)
      imageQr.visible()
    }
    groupInputs.visibility = state.showValues.visibility()
    groupButtons.visibility = state.showValues.not().visibility()
    buttonAddress.text = state.address
    textUrl.text = state.qrString
  }

  override fun onBackPressed(): Boolean = groupInputs.isVisible().also { if (it) back.onNext(Unit) }

}