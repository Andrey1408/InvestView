package ivbackend.tinkoff.account

import ivbackend.tinkoff.error.FigiNotFoundError
import ivbackend.tinkoff.error.TinkoffInternalError
import ivbackend.tinkoff.error.wrapTinkoffRequest
import ivbackend.tinkoff.model.*
import ivbackend.tinkoff.response.OrderState
import ivbackend.tinkoff.response.PositionsResponse
import ru.tinkoff.piapi.contract.v1.OrderDirection
import ru.tinkoff.piapi.core.InvestApi
import java.util.*

open class TinkoffActualAccount(
    token: UserToken,
    private val accountId: AccountId,
) : TinkoffAccount {


    override fun getOpenOrders(): Result<List<OrderState>> {
        val orderStatesFuture = investApi.ordersService
            .getOrders(accountId)

        return wrapTinkoffRequest {
            val orderStates = orderStatesFuture.get()
            orderStates.map(OrderState::fromTinkoff)
        }
    }

    override fun getPositions(): Result<PositionsResponse> {
        val positionsFuture = investApi.operationsService
            .getPositions(accountId)

        return wrapTinkoffRequest {
            val positions = positionsFuture.get()
            PositionsResponse.fromTinkoff(positions)
        }
    }

    override fun getLastPrice(figi: Figi): Result<Quotation> {
        val lastPrices = investApi.marketDataService.getLastPrices(listOf(figi)).get()
        if (lastPrices.size != 1) {
            return Result.failure(TinkoffInternalError())
        }
        val quotation = Quotation.fromTinkoff(lastPrices.first().price)
        if (quotation.isEqualToZero())
            return Result.failure(FigiNotFoundError())
        return Result.success(quotation)
    }

    override fun getLotByShare(figi: Figi): Result<Int> {
        val shareFuture = investApi.instrumentsService
            .getShareByFigi(figi)

        return wrapTinkoffRequest {
            shareFuture.get().lot
        }
    }

    // internal

    private val investApi = InvestApi.createSandbox(token)


}