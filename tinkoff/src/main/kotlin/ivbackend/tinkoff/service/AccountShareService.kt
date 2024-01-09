package ivbackend.tinkoff.service

import ivbackend.db.dao.AccountShareDAO
import ivbackend.tinkoff.model.Portfolio
import io.ktor.features.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
class AccountShareService (
    private val userShareDao: AccountShareDAO,
    private val shareService: ShareService,
    private val userAccountService: AccountService,
) {
    fun getUserPortfolio(accountId: Long) = transaction {
        val account = userAccountService.getAccountOrThrow(accountId)
        val positions = userShareDao.findUserPositions(accountId)

        Portfolio(account, positions)
    }

    fun getUserSharesValue(accountId: Long): BigDecimal {
        return getUserPortfolio(accountId).positions
            .sumOf { it.currentPrice * BigDecimal.valueOf(it.quantity) }
    }

    fun buyShares(accountId: Long, figi: String, quantity: Long) = transaction {
        val account = userAccountService.getAccountOrThrow(accountId)
        val share = shareService.getShareOrThrow(figi)

        if (share.available < quantity) {
            throw BadRequestException("Number of tradable shares $figi less than $quantity")
        }

        val purchasePrice = share.price * BigDecimal.valueOf(quantity)
        if (account.balance < purchasePrice) {
            throw BadRequestException("Not enough fund on the account $accountId")
        }

        userShareDao.addQuantityOrInsert(accountId, figi, quantity)
        userAccountService.withdrawBalance(accountId, purchasePrice)
        shareService.decreaseAvailableShares(share, quantity)
    }

    fun sellShares(accountId: Long, figi: String, quantity: Long) = transaction {
        userAccountService.getAccountOrThrow(accountId)
        val share = shareService.getShareOrThrow(figi)
        val userQuantity: Long? = userShareDao.findUserShareQuantity(accountId, figi)

        if (userQuantity == null || userQuantity < quantity) {
            throw BadRequestException("Not enough shares on the account $accountId")
        }

        userShareDao.addQuantityOrInsert(accountId, figi, -quantity)
        userAccountService.topUpBalance(accountId, BigDecimal.valueOf(quantity) * share.price)
        shareService.increaseAvailableShares(share, quantity)
    }
}