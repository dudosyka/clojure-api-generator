package com.fleetmate.lib.exceptions

import kotlinx.serialization.Serializable

@Serializable
data class NotFoundException (
    override val message: String
): BaseException(404, "Not found", message)