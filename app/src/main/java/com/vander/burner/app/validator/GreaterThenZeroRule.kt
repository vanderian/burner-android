package com.vander.burner.app.validator

import com.vander.burner.R
import com.vander.scaffold.form.validator.ValidateRule
import java.math.BigDecimal

object GreaterThenZeroRule : ValidateRule() {
  override val errorRes: Int
    get() = R.string.error_form_amount

  override fun validate(text: String) = (text.toBigDecimalOrNull() ?: BigDecimal.ZERO) > BigDecimal.ZERO

}