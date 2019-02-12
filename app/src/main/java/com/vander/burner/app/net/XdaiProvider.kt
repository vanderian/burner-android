package com.vander.burner.app.net

import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.di.Xdai
import com.vander.scaffold.annotations.ApplicationScope
import io.reactivex.Observable
import io.reactivex.Single
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import pm.gnosis.ethereum.EthereumRepository
import pm.gnosis.ethereum.models.TransactionParameters
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import pm.gnosis.utils.*
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

// todo missing error code in failure
@ApplicationScope
class XdaiProvider @Inject constructor(
    @Xdai private val xdai: EthereumRepository,
    private val accountRepository: AccountRepository
) {

  private fun trx(to: String, amount: String, msg: String?): Transaction {
    val message = msg.let { if (it.isNullOrBlank()) "0x" else it.toByteArray().toHex().addHexPrefix() }
    return Transaction(to.asEthereumAddress()!!, Wei.ether(amount), data = message)
  }

  private fun gasPrice(tp: TransactionParameters) = if (tp.gasPrice == 0.toBigInteger()) gasPrice else tp.gasPrice

  val balance: Single<Wei>
    get() = xdai.getBalance(accountRepository.address)
        .singleOrError()

  fun createTrx(to: String, amount: String, msg: String?): Single<Transaction> =
      trx(to, amount, msg).let { trx ->
        xdai.getTransactionParameters(accountRepository.address, trx.address, trx.value, trx.data)
            .map { trx.copy(nonce = it.nonce, gasPrice = gasPrice(it), gas = it.gas) }
            .singleOrError()
      }

  fun transfer(trx: Transaction): Single<String> {
    val raw = RawTransaction.createTransaction(trx.nonce, trx.gasPrice, trx.gas, trx.address.asEthereumAddressString(), trx.value?.value, trx.data)
    val signed = TransactionEncoder.signMessage(raw, Credentials.create(ECKeyPair.create(accountRepository.keyPair.privKey)))
    return xdai.sendRawTransaction(signed.toHexString().addHexPrefix()).singleOrError()
  }

  fun receipt(hash: String) = xdai.getTransactionReceipt(hash).singleOrError()

  fun transaction(hash: String) = xdai.getTransactionByHash(hash).singleOrError()

  companion object {
    val gasPrice: BigInteger = BigDecimal(1.1).movePointRight(9).toBigInteger()
  }
}