package com.vander.burner.app.net.model

import com.vander.scaffold.screen.Event

data class ErrorModel(
    val name: String? = null,
    val message: String? = null,
    val resId: Int? = null,
    val args: Array<Any> = emptyArray()
) : Event