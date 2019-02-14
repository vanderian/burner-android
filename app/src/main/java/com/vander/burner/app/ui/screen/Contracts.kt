package com.vander.burner.app.ui.screen

import android.graphics.Bitmap
import android.os.Parcelable
import com.vander.burner.app.ui.ShowDialogEvent
import com.vander.scaffold.form.FormIntents
import com.vander.scaffold.screen.Event
import com.vander.scaffold.screen.NavigationIntent
import com.vander.scaffold.screen.Screen
import io.reactivex.Maybe
import io.reactivex.Observable
import kotlinx.android.parcel.Parcelize
import pm.gnosis.ethereum.models.TransactionReceipt
import pm.gnosis.model.Solidity
import java.math.BigDecimal

@Parcelize
data class TransferData(
    val address: String,
    val amount: String = "",
    val message: String = ""
) : Parcelable

data class UnknownQrCode(val value: String) : Event

data class BalanceState(
    val address: Solidity.Address,
    val balance: BigDecimal = BigDecimal.ZERO
) : Screen.State

interface BalanceIntents : Screen.Intents {
  fun receive(): Observable<Unit>
  fun send(): Observable<Unit>
  fun scan(): Observable<Unit>
  fun settings(): Observable<Unit>
}

data class ReceiveState(
    val qrString: String,
    val qr: Bitmap? = null,
    val showValues: Boolean = false
) : Screen.State

interface ReceiveIntents : NavigationIntent, FormIntents {
  fun copy(): Observable<Unit>
  fun toggle(): Observable<Unit>
}

class SendState() : Screen.State

interface SendIntents : FormIntents, NavigationIntent {
  fun send(): Observable<Unit>
  fun scan(): Observable<Unit>
  fun receipt(data: TransactionReceipt, amount: BigDecimal, hasAction: Boolean): Maybe<Unit>
}

interface ScanIntents : NavigationIntent {
  fun scan(): Observable<String>
}

interface SettingsIntents : NavigationIntent, FormIntents {
  fun burn(): Observable<Unit>
  fun burnConfirm(ev: ShowDialogEvent): Maybe<Unit>
  fun copy(): Observable<Unit>
  fun show(): Observable<Unit>
  fun createFromKey(): Observable<Unit>
  fun createFromSeed(): Observable<Unit>
  fun beer(): Observable<Unit>
}

data class ClipboardEvent(val text: String): Event
data class ShowKeyEvent(val bmp: Bitmap): Event