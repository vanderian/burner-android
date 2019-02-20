package com.vander.burner.app.validator

import com.vander.burner.R
import com.vander.scaffold.form.validator.ValidateRule
import pm.gnosis.utils.hexAsBigIntegerOrNull

object PrivateKeyRule : ValidateRule() {
  override val errorRes: Int
    get() = R.string.error_form_address

  override fun validate(text: String) =
      text.startsWith("0x") && text.length == 66 && text.hexAsBigIntegerOrNull() != null
}