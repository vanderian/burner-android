package com.vander.burner.app.ui.screen

import android.graphics.Bitmap
import android.os.Parcelable
import com.vander.burner.app.ui.ShowDialogEvent
import com.vander.burner.app.ui.ShowReceiptEvent
import com.vander.scaffold.form.FormIntents
import com.vander.scaffold.screen.Event
import com.vander.scaffold.screen.NavigationIntent
import com.vander.scaffold.screen.Screen
import io.reactivex.Maybe
import io.reactivex.Observable
import kotlinx.android.parcel.Parcelize
import pm.gnosis.model.Solidity
import pm.gnosis.models.Wei
import java.math.BigDecimal

@Parcelize
data class TransferData(
    val address: String,
    val amount: String = "",
    val message: String = ""
) : Parcelable, Event

data class UnknownQrCode(val value: String) : Event

data class BalanceState(
    val address: Solidity.Address,
    val pairedAddress: Solidity.Address? = null,
    val balance: BigDecimal = BigDecimal.ZERO
) : Screen.State

interface BalanceIntents : Screen.Intents {
  fun receive(): Observable<Unit>
  fun send(): Observable<Unit>
  fun scan(): Observable<Unit>
  fun settings(): Observable<Unit>
  fun pair(): Observable<Unit>
  fun explorerAddress(): Observable<Unit>
  fun explorerPaired(): Observable<Unit>
}

data class ReceiveState(
    val address: String,
    val qrString: String = "",
    val qr: Bitmap? = null,
    val showValues: Boolean = false
) : Screen.State

interface ReceiveIntents : NavigationIntent, FormIntents {
  fun copy(): Observable<Unit>
  fun toggle(): Observable<Unit>
}

data class SendState(
    val showFab: Boolean
) : Screen.State

interface SendIntents : FormIntents, NavigationIntent {
  fun send(): Observable<Unit>
  fun scan(): Observable<Unit>
  fun receipt(event: ShowReceiptEvent): Maybe<Unit>
}

interface ScanIntents : NavigationIntent {
  fun scan(): Observable<String>
}

data class SettingsState(
    val isEmpty: Boolean = false
): Screen.State

interface SettingsIntents : NavigationIntent, FormIntents {
  fun burn(): Observable<Unit>
  fun burnConfirm(ev: ShowDialogEvent): Maybe<Unit>
  fun copy(): Observable<Unit>
  fun show(): Observable<Unit>
  fun createFromKey(): Observable<Unit>
  fun createFromSeed(): Observable<Unit>
  fun beer(): Observable<Unit>
  fun pair(): Observable<Unit>
}

data class ClipboardEvent(val text: String) : Event
data class ShowKeyEvent(val bmp: Bitmap) : Event

data class PairState(
    val address: String = ""
) : Screen.State

interface PairIntents : NavigationIntent, FormIntents {
  fun pair(): Observable<Unit>
  fun clear(): Observable<Unit>
  fun scan(): Observable<Unit>
}

data class WithdrawState(
    val address: Solidity.Address,
    val balance: Wei = Wei.ZERO
) : Screen.State

interface WithdrawIntents : NavigationIntent, FormIntents {
  fun withdraw(): Observable<Unit>
  fun receipt(event: ShowReceiptEvent): Maybe<Unit>
}