package com.vander.burner.app.validator

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

class DecimalDigitsInputFilter(decimals: Int = 2) : InputFilter {

  private lateinit var pattern: Pattern

  var precision: Int = -1
    set(value) {
      if (field != value) {
        pattern = Pattern.compile("^\\d*[,.]?\\d{0,$value}\$")
        field = value
      }
    }

  init {
    precision = decimals
  }

  override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
    val string = dest.substring(0, dstart) + source + dest.substring(dend)
    return if (pattern.matcher(string).matches()) {
      null
    } else {
      ""
    }
  }
}