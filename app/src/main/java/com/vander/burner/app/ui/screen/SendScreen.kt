package com.vander.burner.app.ui.screen

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.navArgs
import com.jakewharton.rxbinding3.view.clicks
import com.vander.burner.R
import com.vander.burner.app.net.apiHandler
import com.vander.burner.app.ui.animatedClose
import com.vander.burner.app.ui.asEthereumAddressShort
import com.vander.burner.app.validator.DecimalDigitsInputFilter
import com.vander.scaffold.form.FormInput
import com.vander.scaffold.screen.Screen
import com.vander.scaffold.ui.setText
import io.reactivex.Maybe
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_appbar.*
import kotlinx.android.synthetic.main.layout_loading.*
import kotlinx.android.synthetic.main.layout_receipt.view.*
import kotlinx.android.synthetic.main.screen_send.*
import pm.gnosis.ethereum.models.TransactionReceipt
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*

class SendScreen : Screen<SendState, SendIntents>() {
  private lateinit var form: FormInput

  override fun layout(): Int = R.layout.screen_send

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar.setTitle(R.string.title_send)
    toolbar.animatedClose()
    form = FormInput().withTextInputs(inputAddress, inputAmount, inputMessage)
    inputAmount.editText!!.filters = arrayOf(DecimalDigitsInputFilter())
    navArgs<SendScreenArgs>().value.transferData?.let {
      inputAddress.setText(it.address)
      inputAmount.setText(it.amount)
      inputMessage.setText(it.message)
    }
  }

  override fun intents(): SendIntents = object : SendIntents {
    override val form: FormInput
      get() = this@SendScreen.form

    override val toolbar: Toolbar
      get() = this@SendScreen.toolbar

    override fun send(): Observable<Unit> = buttonSend.clicks()
    override fun scan(): Observable<Unit> = fab.clicks()
    override fun receipt(data: TransactionReceipt, amount: BigDecimal, hasAction: Boolean): Maybe<Unit> = receiptDialog(data, amount, hasAction)

    override fun events(): List<Observable<*>> =
        form.events(this@SendScreen) +
            apiHandler { layoutLoading.toggle(it) }
  }

  override fun render(state: SendState) {
  }

  private fun receiptDialog(data: TransactionReceipt, amount: BigDecimal, hasAction: Boolean) = Maybe.create<Unit> { emitter ->
    val view = layoutInflater.inflate(R.layout.layout_receipt, null)
    AlertDialog.Builder(requireContext())
        .setTitle(R.string.title_receipt)
        .setView(view)
        .setOnDismissListener { if (hasAction) emitter.onSuccess(Unit) else emitter.onComplete() }
        .apply {
          if (hasAction) {
            setPositiveButton(R.string.action_scan_next) { _, _ -> emitter.onComplete() }
            setNeutralButton(R.string.action_close) { _, _ -> emitter.onSuccess(Unit) }
          }
        }
        .create()
        .apply {
          emitter.setCancellable { dismiss() }
          view.imageBlockieFrom.setAddress(data.from)
          view.imageBlockieTo.setAddress(data.to)
          view.textAddressFrom.text = data.from.asEthereumAddressShort()
          view.textAddressTo.text = data.to.asEthereumAddressShort()
          view.textAmount.text = NumberFormat.getCurrencyInstance(Locale.US).format(amount)
        }
        .show()
  }
}