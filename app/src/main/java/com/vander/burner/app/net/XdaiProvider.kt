package com.vander.burner.app.net

import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.di.Xdai
import com.vander.scaffold.annotations.ApplicationScope
import io.reactivex.Single
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import pm.gnosis.ethereum.EthGetTransactionCount
import pm.gnosis.ethereum.EthereumRepository
import pm.gnosis.models.Wei
import pm.gnosis.utils.addHexPrefix
import pm.gnosis.utils.asEthereumAddressString
import pm.gnosis.utils.toHex
import pm.gnosis.utils.toHexString
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

// todo replace eth repo with custom rpc, cannot add new requests, missing error code in failure
@ApplicationScope
class XdaiProvider @Inject constructor(
    @Xdai private val xdai: EthereumRepository,
    private val accountRepository: AccountRepository
) {

  private val nonce: Single<BigInteger>
    get() = xdai.request(EthGetTransactionCount(accountRepository.address))
        .map { it.checkedResult() }
        .singleOrError()

  val balance: Single<BigDecimal>
    get() = xdai.getBalance(accountRepository.address)
        .map { it.toEther() }
        .singleOrError()

  fun createTrx(to: String, amount: String, msg: String?) =
      nonce.map {
        val message = msg.let { if (it.isNullOrBlank()) "0x" else it.toByteArray().toHex().addHexPrefix() }
        RawTransaction.createTransaction(it, gasPrice, gas, to.asEthereumAddressString(), Wei.ether(amount).value, message)
      }

  fun transfer(trx: RawTransaction) =
      TransactionEncoder.signMessage(trx, Credentials.create(ECKeyPair.create(accountRepository.keyPair.privKey))).let {
        xdai.sendRawTransaction(it.toHexString().addHexPrefix()).singleOrError()
      }

  fun receipt(hash: String) = xdai.getTransactionReceipt(hash).singleOrError()

  fun trx(hash: String) = xdai.getTransactionByHash(hash).singleOrError()

  companion object {
    val gas = 120000L.toBigInteger()
    val gasPrice: BigInteger = BigDecimal(1.1).movePointRight(9).toBigInteger()
  }
}