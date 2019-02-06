package com.vander.burner.app.net

import com.vander.burner.app.di.XDaiProvider
import com.vander.scaffold.annotations.ApplicationScope
import io.reactivex.Single
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.utils.Convert
import java.math.BigDecimal
import javax.inject.Inject

@ApplicationScope
class Web3Provider @Inject constructor(
    @XDaiProvider private val xdai: Web3j
) {

  val xdaiBalance: Single<BigDecimal>
    get() = Single.fromFuture(xdai.ethGetBalance("0x707a882b400a4ba5be541c2e157bed9e65063e4c", DefaultBlockParameterName.LATEST).sendAsync())
        .map { Convert.fromWei(it.balance.toBigDecimal(), Convert.Unit.ETHER) }

}