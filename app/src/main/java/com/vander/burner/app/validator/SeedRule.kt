package com.vander.burner.app.validator

import com.vander.burner.R
import com.vander.scaffold.form.validator.ValidateRule
import org.web3j.crypto.MnemonicUtils
import pm.gnosis.mnemonic.Bip39
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddress

class SeedRule(private val bip39: Bip39) : ValidateRule() {
  override val errorRes: Int
    get() = R.string.error_form_seed

  override fun validate(text: String) = kotlin.runCatching { bip39.validateMnemonic(text) }.isSuccess
}