package philarmonic.utils.security.jwt

data class RefreshDto (
    val id: Int,
    val lastLogin: Long
)