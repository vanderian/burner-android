package com.vander.burner.app.validator

import com.vander.burner.R
import com.vander.scaffold.form.validator.ValidateRule
import pm.gnosis.models.Wei

class MaxBalanceRule(private val balance: Wei) : ValidateRule() {
  override fun validate(text: String): Boolean = Wei.ether(text) <= balance

  override val errorRes: Int
    get() = R.string.error_form_max_balance
}