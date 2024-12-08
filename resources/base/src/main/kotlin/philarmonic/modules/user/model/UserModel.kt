package philarmonic.modules.user.model


import org.jetbrains.exposed.sql.selectAll
import philarmonic.modules.user.dto.UserDto
import philarmonic.utils.database.BaseIntIdTable

object UserModel : BaseIntIdTable() {
    val login = text("login").nullable().default(null)
    val fullName = text("full_name")
    val hash = text("hash")

    fun getByLogin(login: String): Pair<Int, String> =
        UserModel.select(id, hash).where {
            UserModel.login eq login
        }.map { row ->
            Pair(row[id].value, row[hash])
        }.first()

    fun getOne(id: Int): UserDto? {
        return UserModel.selectAll().where { UserModel.id eq id }.firstOrNull()?.let {
            UserDto(it[this.id].value, it[this.login], it[fullName])
        }
    }
}