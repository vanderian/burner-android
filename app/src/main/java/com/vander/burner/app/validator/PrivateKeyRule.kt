package com.vander.burner.app.validator

import com.vander.burner.R
import com.vander.scaffold.form.validator.ValidateRule
import pm.gnosis.model.Solidity
import pm.gnosis.utils.asEthereumAddress

object PrivateKeyRule : ValidateRule() {
  override val errorRes: Int
    get() = R.string.error_form_address

  override fun validate(text: String) =
      text.asEthereumAddress() != null
}