package com.fleetmate.lib.exceptions

import kotlinx.serialization.Serializable

@Serializable
class UnauthorizedException(): BaseException(401, "Unauthorized")