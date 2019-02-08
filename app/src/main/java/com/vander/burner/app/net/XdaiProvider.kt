package com.vander.burner.app.net

import com.vander.burner.app.data.AccountRepository
import com.vander.burner.app.di.Xdai
import com.vander.scaffold.annotations.ApplicationScope
import io.reactivex.Single
import pm.gnosis.ethereum.EthereumRepository
import java.math.BigDecimal
import javax.inject.Inject

@ApplicationScope
class XdaiProvider @Inject constructor(
    @Xdai private val xdai: EthereumRepository,
    private val accountRepository: AccountRepository
) {

  val balance: Single<BigDecimal>
    get() = xdai.getBalance(accountRepository.address)
        .map { it.toEther() }
        .singleOrError()
}