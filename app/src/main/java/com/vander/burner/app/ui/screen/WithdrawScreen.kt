package com.vander.burner.app.ui.screen

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding3.view.clicks
import com.vander.burner.R
import com.vander.burner.app.net.apiHandler
import com.vander.burner.app.ui.ShowReceiptEvent
import com.vander.burner.app.ui.animatedClose
import com.vander.burner.app.ui.asEthereumAddressShort
import com.vander.burner.app.ui.showReceiptDialog
import com.vander.scaffold.form.FormInput
import com.vander.scaffold.screen.Screen
import io.reactivex.Maybe
import io.reactivex.Observable
import kotlinx.android.synthetic.main.layout_appbar.*
import kotlinx.android.synthetic.main.layout_loading.*
import kotlinx.android.synthetic.main.screen_withdraw.*
import java.text.NumberFormat
import java.util.*

class WithdrawScreen : Screen<WithdrawState, WithdrawIntents>() {
  lateinit var form: FormInput

  override fun layout(): Int = R.layout.screen_withdraw

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    toolbar.setTitle(R.string.title_withdraw)
    toolbar.animatedClose()
    form = FormInput().withTextInputs(inputAmount)
    layoutLoading.show()
  }

  override fun intents(): WithdrawIntents = object : WithdrawIntents {
    override val form: FormInput
      get() = this@WithdrawScreen.form

    override val toolbar: Toolbar
      get() = this@WithdrawScreen.toolbar

    override fun withdraw(): Observable<Unit> = buttonWithdraw.clicks()
    override fun receipt(event: ShowReceiptEvent): Maybe<Unit> = requireContext().showReceiptDialog(event)
    override fun events(): List<Observable<*>> = form.events(this@WithdrawScreen) + apiHandler { layoutLoading.shouldShow(it) }
  }

  override fun render(state: WithdrawState) {
    imageBlockie.setAddress(state.address)
    textAddressShort.text = state.address.asEthereumAddressShort()
    textBalance.text = NumberFormat.getCurrencyInstance(Locale.US).format(state.balance.toEther())
  }

}