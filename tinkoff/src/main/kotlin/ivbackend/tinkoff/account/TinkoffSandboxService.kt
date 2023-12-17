package ivbackend.tinkoff.account

import ivbackend.tinkoff.error.wrapTinkoffRequest
import ivbackend.tinkoff.model.AccountId
import ru.tinkoff.piapi.core.InvestApi

class TinkoffSandboxService(token: String) {

    fun createSandboxAccount(): Result<AccountId> =
        wrapTinkoffRequest {
            sandboxService.openAccount().get()
        }

    fun closeSandboxAccount(accountId: AccountId): Result<Unit> =
        wrapTinkoffRequest {
            sandboxService.closeAccount(accountId).get()
        }

    fun sandboxPayIn(accountId: AccountId, rubles: UInt): Result<Unit> =
        wrapTinkoffRequest {
            val moneyValue = Currency("RUB", Quotation(rubles, 0U)).toMoneyValue()
            sandboxService.payIn(accountId, moneyValue).get()
        }


    private val investApi = InvestApi.createSandbox(token)

    private val sandboxService = investApi.sandboxService
}