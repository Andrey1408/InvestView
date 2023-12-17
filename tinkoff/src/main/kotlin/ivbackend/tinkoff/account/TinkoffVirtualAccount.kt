package ivbackend.tinkoff.account

import ivbackend.tinkoff.error.NotEnoughVirtualMoneyError
import ivbackend.tinkoff.error.NotEnoughVirtualSecurityError
import ivbackend.tinkoff.error.waitForSuccess
import ivbackend.tinkoff.model.*
import ivbackend.tinkoff.response.OrderState
import ivbackend.tinkoff.response.PositionsResponse
import ivbackend.tinkoff.storage.CurrencyStorage
import ivbackend.tinkoff.storage.SecurityStorage

class TinkoffVirtualAccount(
    private val actualAccount: TinkoffActualAccount,
    private val availableCurrencies: CurrencyStorage,
    private val availableSecurities: SecurityStorage,
) : TinkoffAccount {



    override fun getPositions(): Result<PositionsResponse> {
        val virtualPositionsResponse = PositionsResponse(
            availableCurrencies.getAll(),
            availableSecurities.getAll(),
        )
        return Result.success(virtualPositionsResponse)
    }

    override fun getLastPrice(figi: Figi): Result<Quotation> =
        actualAccount.getLastPrice(figi)

    override fun getLotByShare(figi: Figi): Result<Int> =
        actualAccount.getLotByShare(figi)

    fun getTotalBalance(): Result<Quotation> {
        val totalSecuritiesCost = availableSecurities.getAll().map { security ->
            val quantity = security.balance
            val lot = getLotByShare(security.figi)
                .getOrElse { return Result.failure(it) }
            val price = getLastPrice(security.figi)
                .getOrElse { return Result.failure(it) }
            price * quantity * lot.toUInt()
        }.fold(Quotation.zero(), Quotation::plus)
        val currentRubleBalance = availableCurrencies.get("rub")?.quotation ?: Quotation.zero()
        return Result.success(totalSecuritiesCost + currentRubleBalance)
    }

    // internal

    private val myOpenOrders: MutableMap<OrderId, OrderState> =
        mutableMapOf()

    private val myExecutedOrders: MutableMap<OrderId, OrderState> =
        mutableMapOf()



    private fun computeTotalRequestedQuotation(figi: Figi, quantity: UInt, price: Price): Quotation {
        val requestedQuotation = when (price) {
            is LimitedPrice -> price.quotation
            is MarketPrice -> waitForSuccess { actualAccount.getLastPrice(figi) }
        }
        return requestedQuotation * quantity
    }

    private fun computeExtraRequestedQuotation(oldOrder: OrderState, quantity: UInt, price: Price): Quotation {
        val oldRequestedQuotation = oldOrder.totalCost.quotation
        val newRequestedQuotation = computeTotalRequestedQuotation(oldOrder.figi, quantity, price)
        return (newRequestedQuotation - oldRequestedQuotation) ?: Quotation.zero()
    }

    // validation

    private fun validatePostOrder(figi: Figi, quantity: UInt, price: Price): Result<Unit> {
        val requestedCurrency = Currency(
            isoCode = "rub", // TODO
            computeTotalRequestedQuotation(figi, quantity, price)
        )
        if (!availableCurrencies.hasEnough(requestedCurrency))
            return Result.failure(NotEnoughVirtualMoneyError())
        return Result.success(Unit)
    }

    private fun validateSellOrder(figi: Figi, quantity: UInt): Result<Unit> {
        if (!availableSecurities.hasEnough(Security(figi, quantity)))
            return Result.failure(NotEnoughVirtualSecurityError())
        return Result.success(Unit)
    }



    private fun validateReplaceBuyOrder(orderToReplace: OrderState, quantity: UInt, price: Price): Result<Unit> {
        val extraRequestedCurrency = Currency(
            isoCode = "rub", // TODO
            computeExtraRequestedQuotation(orderToReplace, quantity, price)
        )
        if (!availableCurrencies.hasEnough(extraRequestedCurrency)) {
            return Result.failure(NotEnoughVirtualMoneyError())
        }
        return Result.success(Unit)
    }

    private fun validateReplaceSellOrder(orderToReplace: OrderState, quantity: UInt): Result<Unit> {
        if (quantity > orderToReplace.lotsRequested) {
            val extraRequestedQuantity = orderToReplace.lotsRequested - quantity
            if (!availableSecurities.hasEnough(Security(orderToReplace.figi, extraRequestedQuantity))) {
                return Result.failure(NotEnoughVirtualSecurityError())
            }
        }
        return Result.success(Unit)
    }


}