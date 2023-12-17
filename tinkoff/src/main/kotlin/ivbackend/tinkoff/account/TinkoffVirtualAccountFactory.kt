package ivbackend.tinkoff.account

import ivbackend.tinkoff.error.CannotOpenVirtualAccountError
import ivbackend.tinkoff.error.waitForSuccess
import ivbackend.tinkoff.model.*
import ivbackend.tinkoff.response.PositionsResponse
import ivbackend.tinkoff.storage.CurrencyStorage
import ivbackend.tinkoff.storage.SecurityStorage

class TinkoffVirtualAccountFactory(
    private val actualAccount: TinkoffActualAccount,
) {

    fun openVirtualAccount(botUid: BotUid, withAvailablePositions: List<Position>): Result<TinkoffVirtualAccount> =
        tryUpdateAvailablePositions(withAvailablePositions).map {
            val initialCurrencies = withAvailablePositions.filterIsInstance<Currency>()
            val initialSecurities = withAvailablePositions.filterIsInstance<Security>()
            val virtualAccount = TinkoffVirtualAccount(
                actualAccount,
                CurrencyStorage.fromList(initialCurrencies),
                SecurityStorage.fromList(initialSecurities),
            )
            return Result.success(virtualAccount)
        }

    fun closeVirtualAccount(virtualAccount: TinkoffVirtualAccount) {
        val positions = virtualAccount.getPositions().getOrThrow()
         positions.currencies.forEach(availableCurrencies::forceIncrease)
         positions.securities.forEach(availableSecurities::forceIncrease)
    }

    // internal

    private val initialAvailablePositions: PositionsResponse = waitForSuccess {
        actualAccount.getPositions()
    }

    private val availableCurrencies = CurrencyStorage.fromList(initialAvailablePositions.currencies)

    private val availableSecurities = SecurityStorage.fromList(initialAvailablePositions.securities)

    private fun tryUpdateAvailablePositions(requestPositions: List<Position>): Result<Unit> {
        val enoughAvailablePositions = requestPositions.all { position ->
            when (position) {
                is Currency -> availableCurrencies.hasEnough(position)
                is Security -> availableSecurities.hasEnough(position)
            }
        }
        if (!enoughAvailablePositions) {
            return Result.failure(CannotOpenVirtualAccountError("Cannot provide all requested positions"))
        }

        requestPositions.forEach { position ->
            when (position) {
                is Currency -> availableCurrencies.forceDecrease(position)
                is Security -> availableSecurities.forceDecrease(position)
            }
        }

        return Result.success(Unit)
    }
}