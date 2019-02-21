package com.vander.burner.app.validator

import com.vander.burner.R
import com.vander.scaffold.form.validator.ValidateRule
import pm.gnosis.mnemonic.Bip39

class MnemonicRule(private val bip39: Bip39) : ValidateRule() {
    override val errorRes: Int
        get() = R.string.error_form_seed

    override fun validate(text: String) = runCatching { bip39.validateMnemonic(text) }.isSuccess
}