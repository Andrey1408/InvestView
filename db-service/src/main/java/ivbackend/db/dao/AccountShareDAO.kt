package ivbackend.db.dao

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import ivbackend.tinkoff.model.Position

internal object AccountShares : Table("account_shares") {
    val accountId = long("account_id").references(UserAccounts.id)
    val figi = char("figi", 12).references(Shares.figi)
    val quantity = long("quantity")

    override val primaryKey = PrimaryKey(accountId, figi)
}
class AccountShareDAO {
    fun findUserShareQuantity(accountId: Long, figi: String): Long? = transaction {
        AccountShares
            .select { (AccountShares.accountId eq accountId) and (AccountShares.figi eq figi) }
            .map { it[AccountShares.quantity] }
            .singleOrNull()
    }

    fun findUserPositions(accountId: Long): List<Position> = transaction {
        (AccountShares innerJoin Shares)
            .select { (AccountShares.accountId eq accountId) and (AccountShares.quantity greater 0) }
            .map {
                Position(
                    figi = it[AccountShares.figi],
                    quantity = it[AccountShares.quantity],
                    currentPrice = it[Shares.price]
                )
            }.toList()
    }

    fun addQuantityOrInsert(accountId: Long, figi: String, quantityToAdd: Long) = transaction {
        val arguments = listOf(
            AccountShares.accountId to accountId,
            AccountShares.figi to figi,
            AccountShares.quantity to quantityToAdd,
        ).map { (column, arg) -> column.columnType to arg }

        val sql = """
            insert into account_shares (account_id, figi, quantity)
            values (?, ?, ?) 
            on conflict (account_id, figi)
            do update set quantity = user_shares.quantity + excluded.quantity
        """.trimIndent()

        exec(sql, arguments)
    }
}