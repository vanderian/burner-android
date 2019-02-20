package com.vander.burner.app.ui.screen

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxbinding3.view.clicks
import com.vander.burner.R
import com.vander.burner.app.net.apiHandler
import com.vander.burner.app.ui.*
import com.vander.burner.app.validator.DecimalDigitsInputFilter
import com.vander.scaffold.form.FormInput
import com.vander.scaffold.screen.Screen
import com.vander.scaffold.ui.setText
import com.vander.scaffold.ui.visibility
import io.reactivex.Maybe
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_appbar.*
import kotlinx.android.synthetic.main.layout_loading.*
import kotlinx.android.synthetic.main.screen_send.*

class SendScreen : Screen<SendState, SendIntents>() {
  private lateinit var form: FormInput

  override fun layout(): Int = R.layout.screen_send

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar.setTitle(R.string.title_send)
    toolbar.animatedClose()
    form = FormInput().withTextInputs(inputAddress, inputAmount, inputMessage)
    inputAmount.editText!!.filters = arrayOf(DecimalDigitsInputFilter())
  }

  override fun intents(): SendIntents = object : SendIntents {
    override val form: FormInput
      get() = this@SendScreen.form

    override val toolbar: Toolbar
      get() = this@SendScreen.toolbar

    override fun send(): Observable<Unit> = buttonSend.clicks()
    override fun scan(): Observable<Unit> = fab.clicks()
    override fun receipt(event: ShowReceiptEvent): Maybe<Unit> = requireContext().showReceiptDialog(event)

    override fun events(): List<Observable<*>> = listOf(
        event(TransferData::class).doOnNext { fill(it)  }
    ) + form.events(this@SendScreen) + apiHandler { layoutLoading.shouldShow(it) }
  }

  override fun render(state: SendState) {
    fab.visibility = state.showFab.visibility()
  }

  private fun fill(transferData: TransferData) {
    inputAddress.setText(transferData.address)
    inputAmount.setText(transferData.amount)
    inputMessage.setText(transferData.message)
  }
}