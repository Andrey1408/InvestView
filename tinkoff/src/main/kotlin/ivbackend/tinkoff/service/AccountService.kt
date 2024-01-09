package ivbackend.tinkoff.service

import ivbackend.db.dao.AccountDAO
import ivbackend.tinkoff.model.Account
import io.ktor.features.*
import java.math.BigDecimal
class AccountService(
    private val userAccountDao: AccountDAO
) {
    fun getAccountOrThrow(accountId: Long): Account {
        return userAccountDao.find(accountId)
            ?: throw NotFoundException("User account with id $accountId not found")
    }

    fun createAccount(fullName: String, password: String, token: String): Account {
        val initialBalance = BigDecimal.ZERO
        val id = userAccountDao.insertAndGetId(fullName, password, initialBalance, token)
        return Account(id, fullName, password, initialBalance, token)
    }

    fun topUpBalance(accountId: Long, amount: BigDecimal) {
        userAccountDao.addAccountBalance(accountId, amount)
    }

    fun withdrawBalance(accountId: Long, amount: BigDecimal) {
        userAccountDao.addAccountBalance(accountId, amount.negate())
    }
}