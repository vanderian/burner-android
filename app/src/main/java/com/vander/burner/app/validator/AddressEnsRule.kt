package com.vander.burner.app.validator

import com.vander.burner.R
import com.vander.scaffold.form.validator.ValidateRule
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddress
import java.net.IDN

class AddressEnsRule(val address: Solidity.Address) : ValidateRule() {
  override val errorRes: Int
    get() = R.string.error_form_address_ens

  override fun validate(text: String) =
      text.asEthereumAddress().let { it != null && it != address } or
          (runCatching { IDN.toASCII(text, IDN.USE_STD3_ASCII_RULES) }.isSuccess and text.endsWith(".eth") && text.length >= NAME_MIN_LEN)

  companion object {
    private const val NAME_MIN_LEN = 7 + 4 //7name+4suffix
  }
}