package com.vander.burner.app.net

import com.f2prateek.rx.preferences2.Preference
import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.di.Xdai
import com.vander.scaffold.annotations.ApplicationScope
import io.reactivex.Completable
import io.reactivex.Single
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import pm.gnosis.ethereum.*
import pm.gnosis.ethereum.models.TransactionParameters
import pm.gnosis.ethereum.models.TransactionReceipt
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import pm.gnosis.utils.*
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

@ApplicationScope
class XdaiProvider @Inject constructor(
    @Xdai private val xdai: EthereumRepository,
    private val accountRepository: AccountRepository,
    private val pairedSink: Preference<Solidity.Address>
) {

  private val nonceWithBalance: Single<Pair<Wei, BigInteger>>
    get() {
      val nonce = EthGetTransactionCount(accountRepository.address, 1)
      val balance = EthBalance(accountRepository.address, 2)
      return xdai.request(BulkRequest(nonce, balance)).map {
        balance.checkedResult() to nonce.checkedResult()
      }
          .singleOrError()
    }

  private fun gasPrice(tp: TransactionParameters) = tp.gasPrice.max(gasPriceMin)

  private fun receipt(trxToHash: Pair<Transaction, String>) =
      xdai.getTransactionReceipt(trxToHash.second).singleOrError()
          .map { trxToHash.first to it }

  val balance: Single<Wei>
    get() = xdai.getBalance(accountRepository.address)
        .singleOrError()

  fun createTrx(to: String, amount: String, msg: String?): Transaction {
    val message = msg.let { if (it.isNullOrBlank()) "0x" else it.toByteArray().toHex().addHexPrefix() }
    return Transaction(to.asEthereumAddress()!!, Wei.ether(amount), data = message)
  }

  fun prepare(trx: Transaction): Single<Transaction> =
      xdai.getTransactionParameters(accountRepository.address, trx.address, trx.value, trx.data)
          .map { trx.copy(nonce = it.nonce, gasPrice = gasPrice(it), gas = it.gas) }
          .singleOrError()

  fun call(trx: Transaction): Single<Transaction> = xdai.request(EthCall(accountRepository.address, trx))
      .doOnNext { Timber.d(it.checkedResult()) }
      .map { trx }
      .singleOrError()

  fun send(trx: Transaction): Single<Pair<Transaction, String>> {
    val raw = RawTransaction.createTransaction(trx.nonce, trx.gasPrice, trx.gas, trx.address.asEthereumAddressString(), trx.value?.value, trx.data)
    val signed = TransactionEncoder.signMessage(raw, Credentials.create(ECKeyPair.create(accountRepository.keyPair.privKey)))
    return xdai.sendRawTransaction(signed.toHexString().addHexPrefix())
        .map { trx to it }
        .singleOrError()
  }

  fun transfer(trx: Transaction): Single<Pair<Transaction, TransactionReceipt>> =
      prepare(trx)
          .flatMap { call(it) }
          .flatMap { send(it) }
          .flatMap { receipt(it) }

  fun receipt(hash: String) = xdai.getTransactionReceipt(hash).singleOrError()

  fun transaction(hash: String) = xdai.getTransactionByHash(hash).singleOrError()

  fun burn(): Completable = nonceWithBalance
      .map { Transaction(pairedSink.get(), it.first.minusFee(), gasMin, gasPriceMin, "".addHexPrefix(), it.second) }
      .flatMapCompletable {
        if (it.value!!.value <= BigInteger.ZERO) Completable.complete()
        else call(it)
            .flatMap { send(it) }
            .flatMap { receipt(it) }
            .ignoreElement()
      }

  companion object {
    private val gasMin = 21000.toBigInteger()
    private val gasPriceMin: BigInteger = BigDecimal(1.1).movePointRight(9).toBigInteger()

    private fun Wei.minusFee() = minusFee(gasMin, gasPriceMin)
    private fun Wei.minusFee(gas: BigInteger, gasPrice: BigInteger) = Wei(value - gas * gasPrice)
  }
}