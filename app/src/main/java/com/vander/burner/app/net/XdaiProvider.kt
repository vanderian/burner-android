package com.vander.burner.app.net

import com.f2prateek.rx.preferences2.Preference
import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.di.Xdai
import com.vander.burner.app.eth.hash
import com.vander.burner.app.eth.rlp
import com.vander.scaffold.annotations.ApplicationScope
import com.vander.scaffold.screen.Event
import io.reactivex.*
import pm.gnosis.crypto.KeyPair
import pm.gnosis.ethereum.*
import pm.gnosis.ethereum.models.TransactionParameters
import pm.gnosis.ethereum.models.TransactionReceipt
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import pm.gnosis.utils.addHexPrefix
import pm.gnosis.utils.asEthereumAddress
import pm.gnosis.utils.toHex
import pm.gnosis.utils.toHexString
import timber.log.Timber
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject


@ApplicationScope
class XdaiProvider @Inject constructor(
    @Xdai private val xdai: EthereumRepository,
    private val accountRepository: AccountRepository,
    private val pairedSink: Preference<Solidity.Address>
) {

  private fun nonceWithBalance(address: Solidity.Address = accountRepository.address): Single<Pair<Wei, BigInteger>> {
    val nonce = EthGetTransactionCount(address, 1)
    val balance = EthBalance(address, 2)
    return xdai.request(BulkRequest(nonce, balance)).map {
      balance.checkedResult() to nonce.checkedResult()
    }.singleOrError()
  }

  private fun gasPrice(tp: TransactionParameters) = tp.gasPrice.max(gasPriceMin)

  private fun receipt(trxToHash: Pair<Transaction, String>) =
      xdai.getTransactionReceipt(trxToHash.second).singleOrError()
          .retryWhen { errors ->
            val counter = AtomicInteger()
            errors.takeWhile { counter.getAndIncrement() != 3 }
                .flatMap {
                  Timber.d(it)
                  Flowable.timer(BLOCK_TIME, TimeUnit.SECONDS)
                }
          }
          .map { trxToHash.first to it }

  fun balance(address: Solidity.Address = accountRepository.address): Single<Wei> =
      xdai.getBalance(address).singleOrError()

  fun balance(eventObserver: Observer<Event>, address: Solidity.Address = accountRepository.address): Observable<Wei> =
      Observable.interval(BLOCK_TIME, TimeUnit.SECONDS).startWith(0)
          .flatMapMaybe { balance(address).errorHandlingCall(eventObserver) }

  fun isEmptyAccount(address: Solidity.Address = accountRepository.address): Single<Boolean> =
      nonceWithBalance(address).map { (balance, nonce) -> balance == Wei.ZERO && nonce == BigInteger.ZERO }

  fun isEmptyAccount(eventObserver: Observer<Event>, address: Solidity.Address = accountRepository.address) =
      Observable.interval(BLOCK_TIME, TimeUnit.SECONDS)
          .startWith(0)
          .flatMapMaybe { isEmptyAccount(address).errorHandlingCall(eventObserver) }

  fun createTrx(to: String, amount: String, msg: String?): Transaction =
    Transaction(to.asEthereumAddress()!!, Wei.ether(amount), data = msg?.toByteArray()?.toHexString()?.addHexPrefix())

  fun prepare(trx: Transaction, address: Solidity.Address = accountRepository.address): Single<Transaction> =
      xdai.getTransactionParameters(address, trx.address, trx.value, trx.data)
          .map { trx.copy(nonce = it.nonce, gasPrice = gasPrice(it), gas = it.gas) }
          .singleOrError()

  fun call(trx: Transaction, address: Solidity.Address = accountRepository.address): Single<Transaction> =
      xdai.request(EthCall(address, trx, block = Block.LATEST))
          .doOnNext { Timber.d(it.checkedResult()) }
          .map { trx }
          .singleOrError()

  fun sign(trx: Transaction, keyPair: KeyPair = accountRepository.keyPair) =
      trx.rlp(keyPair.sign(trx.hash())).toHexString().addHexPrefix().let { trx to it }

  fun send(signed: Pair<Transaction, String>): Single<Pair<Transaction, String>> =
      xdai.sendRawTransaction(signed.second)
          .map { signed.first to it }
          .singleOrError()

  fun transfer(
      trx: Transaction,
      keyPair: KeyPair = accountRepository.keyPair,
      address: Solidity.Address = accountRepository.address
  ): Single<Pair<Transaction, TransactionReceipt>> =
      prepare(trx, address)
          .flatMap { call(it, address) }
          .map { sign(it, keyPair) }
          .flatMap { send(it) }
          .flatMap { receipt(it) }

  fun receipt(hash: String) = xdai.getTransactionReceipt(hash).singleOrError()

  fun transaction(hash: String) = xdai.getTransactionByHash(hash).singleOrError()

  fun burn(): Completable = nonceWithBalance()
      .map { Transaction(pairedSink.get(), it.first.minusFee(), gasMin, gasPriceMin, "".addHexPrefix(), it.second) }
      .flatMapCompletable {
        if (it.value!!.value <= BigInteger.ZERO) Completable.complete()
        else call(it)
            .map { sign(it) }
            .flatMap { send(it) }
            .flatMap { receipt(it) }
            .ignoreElement()
      }

  companion object {
    const val BLOCK_TIME = 5L

    private val gasMin = 21000.toBigInteger()
    private val gasPriceMin: BigInteger = BigDecimal(1.1).movePointRight(9).toBigInteger()

    private fun Wei.minusFee() = minusFee(gasMin, gasPriceMin)
    private fun Wei.minusFee(gas: BigInteger, gasPrice: BigInteger) = Wei(value - gas * gasPrice)
  }
}