package ivbackend.db.dao


import ivbackend.tinkoff.model.Account
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

internal object UserAccounts : LongIdTable("user_accounts") {
    val username = varchar("urername", 100)
    val password = varchar("password", 10)
    val balance = decimal("balance", 20, 9)
    val token = varchar("token", 100)

    fun toDomain(row: ResultRow): Account {
        return Account(
            id = row[id].value,
            username = row[username],
            password = row[password],
            balance = row[balance],
            token = row[token]
        )
    }
}

class AccountDAO {
    fun insertAndGetId(username: String, password: String, balance: BigDecimal, token: String): Long = transaction {
        UserAccounts.insertAndGetId {
            it[UserAccounts.username] = username
            it[UserAccounts.password] = password
            it[UserAccounts.balance] = balance
            it[UserAccounts.token] = token
        }.value
    }

    fun find(accountId: Long): Account? = transaction {
        UserAccounts
            .select { UserAccounts.id eq accountId }
            .map { UserAccounts.toDomain(it) }
            .singleOrNull()
    }
    fun addAccountBalance(id: Long, amountToAdd: BigDecimal) = transaction {
        UserAccounts.update({ UserAccounts.id eq id }) {
            with(SqlExpressionBuilder) {
                it[balance] = balance + amountToAdd
            }
        }
    }

}