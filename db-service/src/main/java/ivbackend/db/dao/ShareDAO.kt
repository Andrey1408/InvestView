package ivbackend.db.dao

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal
import ivbackend.tinkoff.model.Share

internal object Shares : Table("shares") {
    val figi = char("figi", 12)
    val name = varchar("name", 200)
    val price = decimal("price", 20, 9)
    val available = long("available")

    override val primaryKey = PrimaryKey(figi)

    fun toDomain(row: ResultRow): Share {
        return Share(
            figi = row[figi],
            name = row[name],
            price = row[price],
            available = row[available],
        )
    }
}

class ShareDAO {
    fun insert(figi: String, name: String, price: BigDecimal, available: Long) = transaction {
        Shares.insert {
            it[this.figi] = figi
            it[this.name] = name
            it[this.price] = price
            it[this.available] = available
        }
    }

    fun findAll(): List<Share> = transaction {
        Shares.selectAll().map { Shares.toDomain(it) }.toList()
    }

    fun find(figi: String): Share? = transaction {
        Shares.select { Shares.figi eq figi }
            .map { Shares.toDomain(it) }
            .singleOrNull()
    }

    fun addAvailableQuantity(figi: String, toAdd: Long) = transaction {
        Shares.update({ Shares.figi eq figi }) {
            with(SqlExpressionBuilder) {
                it[available] = available + toAdd
            }
        }
    }
    fun updatePrice(figi: String, price: BigDecimal) = transaction {
        Shares.update({ Shares.figi eq figi }) {
            it[this.price] = price
        }
    }
}