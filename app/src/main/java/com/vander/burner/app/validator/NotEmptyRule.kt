package com.vander.burner.app.validator

import com.vander.burner.R
import com.vander.scaffold.form.validator.ValidateRule

object NotEmptyRule : ValidateRule() {
  override val errorRes: Int
    get() = R.string.error_form_empty

  override fun validate(text: String) = text.isNotBlank()
}