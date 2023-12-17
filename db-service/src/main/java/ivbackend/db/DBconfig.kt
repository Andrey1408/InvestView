package ivbackend.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ivbackend.db.dao.AccountShares
import ivbackend.db.dao.Shares
import ivbackend.db.dao.UserAccounts
fun initDatabase(applicationConfig: ApplicationConfig) {
    val dataSource = HikariDataSource(hikari(applicationConfig))
    val db = Database.connect(dataSource)
    TransactionManager.defaultDatabase = db

}

fun initTables() = transaction {
    SchemaUtils.create(Shares, UserAccounts, AccountShares)
}

private fun hikari(applicationConfig: ApplicationConfig): HikariConfig {
    val hikariConfig = HikariConfig()
    hikariConfig.username = applicationConfig.getHikariProperty("user")
    hikariConfig.password = applicationConfig.getHikariProperty("password")
    hikariConfig.jdbcUrl = applicationConfig.getHikariProperty("jdbcUrl")
    hikariConfig.maximumPoolSize = applicationConfig.getHikariProperty("maximumPoolSize").toInt()
    hikariConfig.validate()
    return hikariConfig
}

private fun ApplicationConfig.getHikariProperty(path: String): String {
    return config("ktor.hikari").property(path).getString()
}