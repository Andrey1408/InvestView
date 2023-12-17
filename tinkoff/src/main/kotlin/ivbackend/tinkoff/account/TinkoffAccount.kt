package ivbackend.tinkoff.account

import ivbackend.tinkoff.model.Figi
import ivbackend.tinkoff.response.PositionsResponse

interface TinkoffAccount {



    fun getPositions(): Result<PositionsResponse>

    fun getLastPrice(figi: Figi): Result<Quotation>

    fun getLotByShare(figi: Figi): Result<Int>
}
