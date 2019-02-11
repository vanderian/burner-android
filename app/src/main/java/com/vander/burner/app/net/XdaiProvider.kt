package com.vander.burner.app.net

import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.di.Xdai
import com.vander.scaffold.annotations.ApplicationScope
import com.vander.scaffold.debug.log
import io.reactivex.Observable
import io.reactivex.Single
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import pm.gnosis.ethereum.EthCall
import pm.gnosis.ethereum.EthGetTransactionCount
import pm.gnosis.ethereum.EthereumRepository
import pm.gnosis.ethereum.models.TransactionParameters
import pm.gnosis.model.Solidity
import pm.gnosis.models.Transaction
import pm.gnosis.models.Wei
import pm.gnosis.utils.*
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

  fun transfer(to: String, amount: String, msg: String?) =
      nonce.flatMap {
        val message = msg.let { if (it.isNullOrBlank()) "0x" else it.toByteArray().toHex().addHexPrefix() }
        val trx = RawTransaction.createTransaction(it, gasPrice, gas, to.asEthereumAddressString(), Wei.ether(amount).value, message)
        val signed = TransactionEncoder.signMessage(trx, Credentials.create(ECKeyPair.create(accountRepository.keyPair.privKey)))
        xdai.sendRawTransaction(signed.toHexString().addHexPrefix()).singleOrError()
      }

  companion object {
    val gas = 120000L.toBigInteger()
    val gasPrice: BigInteger = BigDecimal(1.1).movePointRight(9).toBigInteger()
  }
}