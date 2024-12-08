package philarmonic.modules.auth.data.model


import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import philarmonic.modules.user.model.UserModel
import philarmonic.utils.database.BaseIntIdTable

object UserLoginModel: BaseIntIdTable() {
    val user = reference("user_id", UserModel, ReferenceOption.CASCADE, ReferenceOption.CASCADE)
    val lastLogin = long("last_login")

    fun getLastLogin(userId: Int): Long? {
        return select(lastLogin).where { user eq userId }.firstOrNull()?.get(lastLogin)
    }

    fun setLastLogin(userId: Int, lastLogin: Long) =
        if (getLastLogin(userId) == null) {
            insert {
                it[user] = userId
                it[this.lastLogin] = lastLogin
            }.insertedCount
        } else {
            update({ UserLoginModel.user eq userId }) {
                it[this.lastLogin] = lastLogin
            }
        }
}