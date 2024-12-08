package com.fleetmate.lib.exceptions

import kotlinx.serialization.Serializable

@Serializable
sealed class BaseException (
    val httpStatusCode: Int,
    val httpStatusText: String,
    var data: String? = null
): Exception(data)
